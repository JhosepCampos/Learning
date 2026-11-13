package com.cibertec.learning.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cibertec.learning.datos.BaseDatosLocalHelper
import com.cibertec.learning.datos.FirebaseServicio
import com.cibertec.learning.datos.OracionDao
import com.cibertec.learning.datos.RepositorioOraciones
import com.cibertec.learning.modelo.OracionModelo
import com.cibertec.learning.utilidades.RedUtilidad
import com.cibertec.learning.utilidades.VozUtilidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para la pantalla de oraciones de comunicación rápida.
 */
class OracionesViewModel(
    aplicacion: Application,
    private val repositorio: RepositorioOraciones,
    private val vozUtilidad: VozUtilidad
) : AndroidViewModel(aplicacion) {

    private val _oraciones = MutableStateFlow<List<OracionModelo>>(emptyList())
    val oraciones: StateFlow<List<OracionModelo>> = _oraciones.asStateFlow()

    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    private val _contextoSeleccionado = MutableStateFlow("todos")
    val contextoSeleccionado: StateFlow<String> = _contextoSeleccionado.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _totalOraciones = MutableStateFlow(0)
    val totalOraciones: StateFlow<Int> = _totalOraciones.asStateFlow()

    private val _mensajeNotificacion = MutableStateFlow<String?>(null)
    val mensajeNotificacion: StateFlow<String?> = _mensajeNotificacion.asStateFlow()

    init {
        cargarOraciones()
    }

    /**
     * Carga las oraciones según el contexto y el término de búsqueda activos.
     */
    fun cargarOraciones() {
        viewModelScope.launch {
            _estaCargando.value = true
            val lista = if (_terminoBusqueda.value.isBlank()) {
                repositorio.obtenerOraciones(_contextoSeleccionado.value)
            } else {
                repositorio.buscarOraciones(_terminoBusqueda.value, _contextoSeleccionado.value)
            }
            _oraciones.value = lista
            _totalOraciones.value = repositorio.contarOraciones()
            _estaCargando.value = false
        }
    }

    /**
     * Actualiza el término de búsqueda y recarga.
     */
    fun actualizarBusqueda(termino: String) {
        _terminoBusqueda.value = termino
        cargarOraciones()
    }

    /**
     * Cambia el contexto de filtro y recarga.
     */
    fun seleccionarContexto(contexto: String) {
        _contextoSeleccionado.value = contexto
        cargarOraciones()
    }

    /**
     * Guarda una oración nueva o actualiza una existente.
     */
    fun guardarOracion(
        idExistente: String?,
        textoIngles: String,
        pronunciacionFonetica: String,
        traduccion: String,
        contexto: String = "general",
        alCompletar: () -> Unit
    ) {
        if (textoIngles.isBlank() || traduccion.isBlank()) {
            _mensajeNotificacion.value = "El texto en inglés y la traducción son obligatorios"
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            val oracion = OracionModelo(
                id = idExistente ?: UUID.randomUUID().toString(),
                textoIngles = textoIngles.trim(),
                pronunciacionFonetica = pronunciacionFonetica.trim(),
                traduccion = traduccion.trim(),
                contexto = contexto,
                fechaCreacion = System.currentTimeMillis()
            )
            repositorio.guardarOracion(oracion)
            _mensajeNotificacion.value = "Oración guardada en tu cuaderno"
            _estaCargando.value = false
            cargarOraciones()
            alCompletar()
        }
    }

    /**
     * Elimina una oración por su ID.
     */
    fun eliminarOracion(id: String) {
        viewModelScope.launch {
            repositorio.eliminarOracion(id)
            _mensajeNotificacion.value = "Oración eliminada"
            cargarOraciones()
        }
    }

    /**
     * Pronuncia la oración en inglés usando el TTS nativo (sin API externa).
     */
    fun pronunciarEnIngles(texto: String) {
        vozUtilidad.pronunciarEnIngles(texto)
    }

    /**
     * Refresca al cambiar sesión de usuario.
     */
    fun refrescarDatosUsuario() {
        cargarOraciones()
    }

    fun limpiarMensaje() {
        _mensajeNotificacion.value = null
    }

    override fun onCleared() {
        super.onCleared()
        vozUtilidad.liberarRecursos()
    }

    companion object {
        fun crearFabrica(aplicacion: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dbHelper = BaseDatosLocalHelper(aplicacion)
                    val dao = OracionDao(dbHelper)
                    val firebaseServicio = FirebaseServicio()
                    val redUtilidad = RedUtilidad(aplicacion)
                    val vozUtilidad = VozUtilidad(aplicacion)
                    val repositorio = RepositorioOraciones(dao, firebaseServicio, redUtilidad)

                    return OracionesViewModel(aplicacion, repositorio, vozUtilidad) as T
                }
            }
        }
    }
}

