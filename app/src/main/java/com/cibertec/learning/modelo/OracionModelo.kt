package com.cibertec.learning.modelo

import java.util.UUID

/**
 * Representa una oración o frase formada por el usuario en su cuaderno de aprendizaje.
 * Cuenta con su texto en inglés, pronunciación fonética, traducción al español y contexto.
 */
data class OracionModelo(
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "",
    val textoIngles: String = "",
    val pronunciacionFonetica: String = "",
    val traduccion: String = "",
    val contexto: String = "cotidiano",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false,
    val eliminado: Boolean = false
) {
    /**
     * Convierte el modelo a mapa para almacenar en Cloud Firestore.
     */
    fun aMapaFirestore(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "usuarioId" to usuarioId,
            "textoIngles" to textoIngles,
            "pronunciacionFonetica" to pronunciacionFonetica,
            "traduccion" to traduccion,
            "contexto" to contexto,
            "fechaCreacion" to fechaCreacion,
            "eliminado" to eliminado
        )
    }

    companion object {
        /**
         * Reconstruye una instancia de OracionModelo desde Firestore.
         */
        fun desdeMapaFirestore(id: String, mapa: Map<String, Any?>): OracionModelo {
            return OracionModelo(
                id = id,
                usuarioId = mapa["usuarioId"] as? String ?: "",
                textoIngles = mapa["textoIngles"] as? String ?: "",
                pronunciacionFonetica = mapa["pronunciacionFonetica"] as? String ?: "",
                traduccion = mapa["traduccion"] as? String ?: "",
                contexto = mapa["contexto"] as? String ?: "cotidiano",
                fechaCreacion = (mapa["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                sincronizado = true,
                eliminado = mapa["eliminado"] as? Boolean ?: false
            )
        }
    }
}
