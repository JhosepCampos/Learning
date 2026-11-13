package com.cibertec.learning.modelo

/**
 * Representa al usuario autenticado en la aplicación.
 */
data class UsuarioModelo(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = ""
)
