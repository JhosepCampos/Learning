package com.cibertec.learning.datos

import com.cibertec.learning.modelo.PalabraModelo
import com.cibertec.learning.utilidades.RedUtilidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio central que implementa el patrón Offline-First.
 * Filtra siempre por el usuario autenticado para asegurar que cada usuario
 * vea única y exclusivamente sus propios registros, tanto en SQLite local como en Cloud Firestore.
 */
class RepositorioVocabulario(
    private val palabraDao: PalabraDao,
    private val firebaseServicio: FirebaseServicio,
    private val redUtilidad: RedUtilidad
) {

    /**
     * Obtiene el identificador del usuario activo.
     */
    fun obtenerIdUsuarioActivo(): String {
        return firebaseServicio.obtenerUsuarioActual()?.uid.orEmpty()
    }

    /**
     * Guarda o actualiza una palabra en el cuaderno del usuario.
     */
    suspend fun guardarPalabra(palabra: PalabraModelo): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual()
        val uid = usuario?.uid ?: palabra.usuarioId
        val palabraConUsuario = palabra.copy(usuarioId = uid)
        val tieneInternet = redUtilidad.hayConexionInternet()

        if (usuario != null && tieneInternet) {
            val subidaExitosa = firebaseServicio.sincronizarPalabraEnFirestore(
                usuario.uid,
                palabraConUsuario.copy(sincronizado = true)
            )
            palabraDao.guardarOActualizar(palabraConUsuario.copy(sincronizado = subidaExitosa))
        } else {
            // Guardado local en SQLite marcado como pendiente de sincronización
            palabraDao.guardarOActualizar(palabraConUsuario.copy(sincronizado = false))
        }
    }

    /**
     * Obtiene palabras del cuaderno local SQLite del usuario activo, filtradas por categoría.
     */
    suspend fun obtenerPalabras(categoria: String): List<PalabraModelo> = withContext(Dispatchers.IO) {
        val uid = obtenerIdUsuarioActivo()
        if (uid.isBlank()) return@withContext emptyList()
        palabraDao.obtenerPorCategoria(uid, categoria)
    }

    /**
     * Busca palabras en el cuaderno local SQLite del usuario activo.
     */
    suspend fun buscarPalabras(termino: String, categoria: String): List<PalabraModelo> = withContext(Dispatchers.IO) {
        val uid = obtenerIdUsuarioActivo()
        if (uid.isBlank()) return@withContext emptyList()
        if (termino.isBlank()) {
            palabraDao.obtenerPorCategoria(uid, categoria)
        } else {
            palabraDao.buscarPalabras(uid, termino, categoria)
        }
    }

    /**
     * Obtiene una palabra por su ID para el usuario activo.
     */
    suspend fun obtenerPalabraPorId(id: String): PalabraModelo? = withContext(Dispatchers.IO) {
        val uid = obtenerIdUsuarioActivo()
        if (uid.isBlank()) return@withContext null
        palabraDao.obtenerPorId(id, uid)
    }

    /**
     * Elimina una palabra del cuaderno del usuario activo.
     */
    suspend fun eliminarPalabra(id: String): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual()
        val uid = usuario?.uid ?: return@withContext false
        val tieneInternet = redUtilidad.hayConexionInternet()

        if (tieneInternet) {
            firebaseServicio.eliminarPalabraDeFirestore(uid, id)
            palabraDao.eliminarDefinitivo(id, uid)
        } else {
            palabraDao.marcarComoEliminada(id, uid)
        }
    }

    /**
     * Cuenta la cantidad de palabras registradas en una categoría por el usuario activo.
     */
    suspend fun contarPorCategoria(categoria: String): Int = withContext(Dispatchers.IO) {
        val uid = obtenerIdUsuarioActivo()
        if (uid.isBlank()) return@withContext 0
        palabraDao.contarPorCategoria(uid, categoria)
    }

    /**
     * Obtiene la cantidad de cambios pendientes de sincronizar con Firebase del usuario activo.
     */
    suspend fun contarPendientesSincronizacion(): Int = withContext(Dispatchers.IO) {
        val uid = obtenerIdUsuarioActivo()
        if (uid.isBlank()) return@withContext 0
        palabraDao.obtenerPendientesDeSincronizar(uid).size
    }

    /**
     * Realiza la sincronización bidireccional entre SQLite y Cloud Firestore exclusivamente para el usuario activo.
     */
    suspend fun sincronizarConFirebase(): Boolean = withContext(Dispatchers.IO) {
        val usuario = firebaseServicio.obtenerUsuarioActual() ?: return@withContext false
        val uid = usuario.uid
        if (!redUtilidad.hayConexionInternet()) return@withContext false

        try {
            // 1. Subir cambios locales pendientes del usuario a su subcolección en Firestore
            val pendientesLocales = palabraDao.obtenerPendientesDeSincronizar(uid)
            for (palabra in pendientesLocales) {
                if (palabra.eliminado) {
                    val eliminadoRemoto = firebaseServicio.eliminarPalabraDeFirestore(uid, palabra.id)
                    if (eliminadoRemoto) {
                        palabraDao.eliminarDefinitivo(palabra.id, uid)
                    }
                } else {
                    val subidaExitosa = firebaseServicio.sincronizarPalabraEnFirestore(
                        uid,
                        palabra.copy(sincronizado = true)
                    )
                    if (subidaExitosa) {
                        palabraDao.marcarComoSincronizado(palabra.id, uid)
                    }
                }
            }

            // 2. Descargar palabras remotas de Firestore a SQLite para este usuario
            val palabrasRemotas = firebaseServicio.descargarPalabrasDeFirestore(uid)
            for (remota in palabrasRemotas) {
                val local = palabraDao.obtenerPorId(remota.id, uid)
                if (local == null || local.sincronizado) {
                    if (remota.eliminado) {
                        palabraDao.eliminarDefinitivo(remota.id, uid)
                    } else {
                        palabraDao.guardarOActualizar(remota.copy(usuarioId = uid, sincronizado = true))
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
