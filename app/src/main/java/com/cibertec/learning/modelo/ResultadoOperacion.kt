package com.cibertec.learning.modelo

/**
 * Clase sellada para representar el estado de operaciones asíncronas.
 */
sealed class ResultadoOperacion<out T> {
    data object Inactivo : ResultadoOperacion<Nothing>()
    data object Cargando : ResultadoOperacion<Nothing>()
    data class Exito<T>(val datos: T) : ResultadoOperacion<T>()
    data class Error(val mensaje: String) : ResultadoOperacion<Nothing>()
}
