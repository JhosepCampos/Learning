package com.cibertec.learning.modelo

import java.util.UUID

/**
 * Representa una entrada de vocabulario en el cuaderno de inglés del usuario.
 * Incluye usuarioId para garantizar que cada usuario tenga acceso exclusivo a sus propios registros.
 */
data class PalabraModelo(
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "",
    val textoIngles: String = "",
    val pronunciacionFonetica: String = "",
    val traduccion: String = "",
    val categoria: String = CategoriaModelo.VERBOS.identificador,
    val ejemplo: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false,
    val eliminado: Boolean = false
) {
    /**
     * Convierte el modelo a un mapa para guardarlo en Cloud Firestore.
     */
    fun aMapaFirestore(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "usuarioId" to usuarioId,
            "textoIngles" to textoIngles,
            "pronunciacionFonetica" to pronunciacionFonetica,
            "traduccion" to traduccion,
            "categoria" to categoria,
            "ejemplo" to ejemplo,
            "fechaCreacion" to fechaCreacion,
            "eliminado" to eliminado
        )
    }

    companion object {
        /**
         * Crea una instancia de PalabraModelo a partir de un mapa de Cloud Firestore.
         */
        fun desdeMapaFirestore(id: String, mapa: Map<String, Any?>): PalabraModelo {
            return PalabraModelo(
                id = id,
                usuarioId = mapa["usuarioId"] as? String ?: "",
                textoIngles = mapa["textoIngles"] as? String ?: "",
                pronunciacionFonetica = mapa["pronunciacionFonetica"] as? String ?: "",
                traduccion = mapa["traduccion"] as? String ?: "",
                categoria = mapa["categoria"] as? String ?: CategoriaModelo.VERBOS.identificador,
                ejemplo = mapa["ejemplo"] as? String ?: "",
                fechaCreacion = (mapa["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                sincronizado = true,
                eliminado = mapa["eliminado"] as? Boolean ?: false
            )
        }
    }
}
