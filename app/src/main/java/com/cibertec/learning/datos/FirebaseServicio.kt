package com.cibertec.learning.datos

import com.cibertec.learning.modelo.PalabraModelo
import com.cibertec.learning.modelo.ResultadoOperacion
import com.cibertec.learning.modelo.UsuarioModelo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Servicio encargado de la comunicación con Firebase Authentication y Cloud Firestore.
 */
class FirebaseServicio(
    private val autenticacion: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Retorna el usuario actualmente autenticado o null si no hay sesión.
     */
    fun obtenerUsuarioActual(): UsuarioModelo? {
        val usuarioFirebase = autenticacion.currentUser ?: return null
        return UsuarioModelo(
            uid = usuarioFirebase.uid,
            correo = usuarioFirebase.email ?: "",
            nombre = usuarioFirebase.displayName ?: (usuarioFirebase.email?.substringBefore("@") ?: "")
        )
    }

    /**
     * Inicia sesión con correo y contraseña.
     */
    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoOperacion<UsuarioModelo> {
        return try {
            val resultado = autenticacion.signInWithEmailAndPassword(correo, contrasena).await()
            val usuarioFirebase = resultado.user
            if (usuarioFirebase != null) {
                val modelo = UsuarioModelo(
                    uid = usuarioFirebase.uid,
                    correo = usuarioFirebase.email ?: "",
                    nombre = usuarioFirebase.displayName ?: (usuarioFirebase.email?.substringBefore("@") ?: "")
                )
                ResultadoOperacion.Exito(modelo)
            } else {
                ResultadoOperacion.Error("No se pudo obtener información del usuario")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error(e.localizedMessage ?: "Error al iniciar sesión")
        }
    }

    /**
     * Registra un nuevo usuario con correo y contraseña.
     */
    suspend fun registrarUsuario(correo: String, contrasena: String): ResultadoOperacion<UsuarioModelo> {
        return try {
            val resultado = autenticacion.createUserWithEmailAndPassword(correo, contrasena).await()
            val usuarioFirebase = resultado.user
            if (usuarioFirebase != null) {
                val modelo = UsuarioModelo(
                    uid = usuarioFirebase.uid,
                    correo = usuarioFirebase.email ?: "",
                    nombre = usuarioFirebase.displayName ?: (usuarioFirebase.email?.substringBefore("@") ?: "")
                )
                ResultadoOperacion.Exito(modelo)
            } else {
                ResultadoOperacion.Error("No se pudo registrar el usuario")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error(e.localizedMessage ?: "Error al registrar usuario")
        }
    }

    /**
     * Cierra la sesión activa en Firebase.
     */
    fun cerrarSesion() {
        autenticacion.signOut()
    }

    /**
     * Guarda o actualiza una palabra en la subcolección de Firestore del usuario.
     */
    suspend fun sincronizarPalabraEnFirestore(uid: String, palabra: PalabraModelo): Boolean {
        return try {
            firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_PALABRAS)
                .document(palabra.id)
                .set(palabra.aMapaFirestore(), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina físicamente una palabra de Firestore.
     */
    suspend fun eliminarPalabraDeFirestore(uid: String, palabraId: String): Boolean {
        return try {
            firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_PALABRAS)
                .document(palabraId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Descarga todas las palabras del usuario guardadas en Cloud Firestore.
     */
    suspend fun descargarPalabrasDeFirestore(uid: String): List<PalabraModelo> {
        return try {
            val snapshot = firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_PALABRAS)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { datos ->
                    PalabraModelo.desdeMapaFirestore(doc.id, datos)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Guarda o actualiza una oración en la subcolección de Firestore del usuario.
     */
    suspend fun sincronizarOracionEnFirestore(uid: String, oracion: com.cibertec.learning.modelo.OracionModelo): Boolean {
        return try {
            firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_ORACIONES)
                .document(oracion.id)
                .set(oracion.aMapaFirestore(), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina una oración de Firestore.
     */
    suspend fun eliminarOracionDeFirestore(uid: String, oracionId: String): Boolean {
        return try {
            firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_ORACIONES)
                .document(oracionId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Descarga todas las oraciones del usuario guardadas en Firestore.
     */
    suspend fun descargarOracionesDeFirestore(uid: String): List<com.cibertec.learning.modelo.OracionModelo> {
        return try {
            val snapshot = firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_ORACIONES)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { datos ->
                    com.cibertec.learning.modelo.OracionModelo.desdeMapaFirestore(doc.id, datos)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val COLECCION_USUARIOS = "usuarios"
        private const val COLECCION_PALABRAS = "palabras"
        private const val COLECCION_ORACIONES = "oraciones"
    }
}
