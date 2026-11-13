package com.cibertec.learning.datos

import com.cibertec.learning.modelo.OracionModelo
import com.cibertec.learning.utilidades.RedUtilidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para oraciones de comunicación rápida.
 * Sigue el patrón Offline-First: lee/escribe siempre en SQLite local
 * y sincroniza con Cloud Firestore cuando hay conexión a internet.
 * Cada operación filtra estrictamente por el usuario activo.
 */
class RepositorioOraciones(
    private val oracionDao: OracionDao,
    private val firebaseServicio: FirebaseServicio,
    private val redUtilidad: RedUtilidad
) {

    private fun obtenerUidActivo(): String =
        firebaseServicio.obtenerUsuarioActual()?.uid.orEmpty()

    /**
     * Guarda o actualiza una oración asignándola siempre al usuario activo.
     */
    suspend fun guardarOracion(oracion: OracionModelo): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual()
        val uid = usuario?.uid ?: oracion.usuarioId
        val oracionConUsuario = oracion.copy(usuarioId = uid)
        val tieneInternet = redUtilidad.hayConexionInternet()

        if (usuario != null && tieneInternet) {
            val subidaExitosa = firebaseServicio.sincronizarOracionEnFirestore(
                uid, oracionConUsuario.copy(sincronizado = true)
            )
            oracionDao.guardarOActualizar(oracionConUsuario.copy(sincronizado = subidaExitosa))
        } else {
            oracionDao.guardarOActualizar(oracionConUsuario.copy(sincronizado = false))
        }
    }

    /**
     * Obtiene oraciones del usuario activo, opcionalmente filtradas por contexto.
     */
    suspend fun obtenerOraciones(contexto: String = "todos"): List<OracionModelo> = withContext(Dispatchers.IO) {
        val uid = obtenerUidActivo()
        if (uid.isBlank()) return@withContext emptyList()
        oracionDao.obtenerPorContexto(uid, contexto)
    }

    /**
     * Busca oraciones por texto en inglés o traducción.
     */
    suspend fun buscarOraciones(termino: String, contexto: String?): List<OracionModelo> = withContext(Dispatchers.IO) {
        val uid = obtenerUidActivo()
        if (uid.isBlank()) return@withContext emptyList()
        if (termino.isBlank()) {
            oracionDao.obtenerPorContexto(uid, contexto ?: "todos")
        } else {
            oracionDao.buscarOraciones(uid, termino, contexto)
        }
    }

    /**
     * Obtiene una oración por su ID.
     */
    suspend fun obtenerOracionPorId(id: String): OracionModelo? = withContext(Dispatchers.IO) {
        val uid = obtenerUidActivo()
        if (uid.isBlank()) return@withContext null
        oracionDao.obtenerPorId(id, uid)
    }

    /**
     * Elimina una oración del cuaderno del usuario activo.
     */
    suspend fun eliminarOracion(id: String): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual()
        val uid = usuario?.uid ?: return@withContext false
        val tieneInternet = redUtilidad.hayConexionInternet()

        if (tieneInternet) {
            firebaseServicio.eliminarOracionDeFirestore(uid, id)
            oracionDao.eliminarDefinitivo(id, uid)
        } else {
            oracionDao.marcarComoEliminada(id, uid)
        }
    }

    /**
     * Cuenta el total de oraciones del usuario activo.
     */
    suspend fun contarOraciones(): Int = withContext(Dispatchers.IO) {
        val uid = obtenerUidActivo()
        if (uid.isBlank()) return@withContext 0
        oracionDao.contarOraciones(uid)
    }

    /**
     * Sincronización bidireccional SQLite ↔ Firestore para oraciones.
     */
    suspend fun sincronizarConFirebase(): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual() ?: return@withContext false
        val uid = usuario.uid
        if (!redUtilidad.hayConexionInternet()) return@withContext false

        try {
            val pendientes = oracionDao.obtenerPendientesDeSincronizar(uid)
            for (oracion in pendientes) {
                if (oracion.eliminado) {
                    if (firebaseServicio.eliminarOracionDeFirestore(uid, oracion.id)) {
                        oracionDao.eliminarDefinitivo(oracion.id, uid)
                    }
                } else {
                    if (firebaseServicio.sincronizarOracionEnFirestore(uid, oracion.copy(sincronizado = true))) {
                        oracionDao.marcarComoSincronizado(oracion.id, uid)
                    }
                }
            }

            val remotas = firebaseServicio.descargarOracionesDeFirestore(uid)
            for (remota in remotas) {
                val local = oracionDao.obtenerPorId(remota.id, uid)
                if (local == null || local.sincronizado) {
                    if (remota.eliminado) {
                        oracionDao.eliminarDefinitivo(remota.id, uid)
                    } else {
                        oracionDao.guardarOActualizar(remota.copy(usuarioId = uid, sincronizado = true))
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

