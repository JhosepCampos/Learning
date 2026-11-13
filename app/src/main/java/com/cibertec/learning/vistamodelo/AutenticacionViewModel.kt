package com.cibertec.learning.vistamodelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.learning.datos.FirebaseServicio
import com.cibertec.learning.modelo.ResultadoOperacion
import com.cibertec.learning.modelo.UsuarioModelo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado del flujo de autenticación con Firebase.
 */
class AutenticacionViewModel(
    private val firebaseServicio: FirebaseServicio = FirebaseServicio()
) : ViewModel() {

    private val _estadoAutenticacion = MutableStateFlow<ResultadoOperacion<UsuarioModelo>>(ResultadoOperacion.Inactivo)
    val estadoAutenticacion: StateFlow<ResultadoOperacion<UsuarioModelo>> = _estadoAutenticacion.asStateFlow()

    private val _usuarioActual = MutableStateFlow<UsuarioModelo?>(firebaseServicio.obtenerUsuarioActual())
    val usuarioActual: StateFlow<UsuarioModelo?> = _usuarioActual.asStateFlow()

    fun actualizarUsuarioActual() {
        _usuarioActual.value = firebaseServicio.obtenerUsuarioActual()
    }

    /**
     * Inicia sesión en Firebase Authentication.
     */
    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _estadoAutenticacion.value = ResultadoOperacion.Error("Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _estadoAutenticacion.value = ResultadoOperacion.Cargando
            val resultado = firebaseServicio.iniciarSesion(correo.trim(), contrasena)
            _estadoAutenticacion.value = resultado
            if (resultado is ResultadoOperacion.Exito) {
                _usuarioActual.value = resultado.datos
            }
        }
    }

    /**
     * Registra un nuevo usuario en Firebase Authentication.
     */
    fun registrarUsuario(correo: String, contrasena: String, confirmarContrasena: String) {
        if (correo.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
            _estadoAutenticacion.value = ResultadoOperacion.Error("Completa todos los campos")
            return
        }
        if (contrasena.length < 6) {
            _estadoAutenticacion.value = ResultadoOperacion.Error("La contraseña debe tener mínimo 6 caracteres")
            return
        }
        if (contrasena != confirmarContrasena) {
            _estadoAutenticacion.value = ResultadoOperacion.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _estadoAutenticacion.value = ResultadoOperacion.Cargando
            val resultado = firebaseServicio.registrarUsuario(correo.trim(), contrasena)
            _estadoAutenticacion.value = resultado
            if (resultado is ResultadoOperacion.Exito) {
                _usuarioActual.value = resultado.datos
            }
        }
    }

    /**
     * Cierra la sesión activa.
     */
    fun cerrarSesion() {
        firebaseServicio.cerrarSesion()
        _usuarioActual.value = null
        _estadoAutenticacion.value = ResultadoOperacion.Inactivo
    }

    /**
     * Reinicia el estado de las alertas o errores.
     */
    fun limpiarEstado() {
        _estadoAutenticacion.value = ResultadoOperacion.Inactivo
    }
}
