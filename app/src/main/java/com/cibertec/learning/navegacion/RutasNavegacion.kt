package com.cibertec.learning.navegacion

/**
 * Define las rutas de navegación de la aplicación en español.
 */
sealed class RutasNavegacion(val ruta: String) {
    data object Inicio : RutasNavegacion("pantalla_inicio")

    data object ListaVocabulario : RutasNavegacion("pantalla_lista/{categoria}") {
        fun crearRuta(categoria: String): String = "pantalla_lista/$categoria"
    }

    data object FormularioPalabra : RutasNavegacion("pantalla_formulario?id={id}&categoriaPredeterminada={categoriaPredeterminada}") {
        fun crearRuta(id: String? = null, categoriaPredeterminada: String? = null): String {
            val paramId = id ?: ""
            val paramCat = categoriaPredeterminada ?: ""
            return "pantalla_formulario?id=$paramId&categoriaPredeterminada=$paramCat"
        }
    }

    data object Oraciones : RutasNavegacion("pantalla_oraciones")

    data object FormularioOracion : RutasNavegacion("pantalla_formulario_oracion?id={id}") {
        fun crearRuta(id: String? = null): String {
            return "pantalla_formulario_oracion?id=${id ?: ""}"
        }
    }

    data object Login : RutasNavegacion("pantalla_login")
    data object Registro : RutasNavegacion("pantalla_registro")
}
