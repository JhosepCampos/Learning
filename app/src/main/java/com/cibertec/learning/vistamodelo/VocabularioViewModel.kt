package com.cibertec.learning.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cibertec.learning.datos.BaseDatosLocalHelper
import com.cibertec.learning.datos.FirebaseServicio
import com.cibertec.learning.datos.PalabraDao
import com.cibertec.learning.datos.RepositorioVocabulario
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.modelo.PalabraModelo
import com.cibertec.learning.utilidades.RedUtilidad
import com.cibertec.learning.utilidades.VozUtilidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal para la gestión del vocabulario en el cuaderno de inglés.
 */
class VocabularioViewModel(
    aplicacion: Application,
    private val repositorio: RepositorioVocabulario,
    private val redUtilidad: RedUtilidad,
    private val vozUtilidad: VozUtilidad
) : AndroidViewModel(aplicacion) {

    private val _palabras = MutableStateFlow<List<PalabraModelo>>(emptyList())
    val palabras: StateFlow<List<PalabraModelo>> = _palabras.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow(CategoriaModelo.TODOS.identificador)
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _estaSincronizando = MutableStateFlow(false)
    val estaSincronizando: StateFlow<Boolean> = _estaSincronizando.asStateFlow()

    private val _hayConexion = MutableStateFlow(redUtilidad.hayConexionInternet())
    val hayConexion: StateFlow<Boolean> = _hayConexion.asStateFlow()

    private val _conteosPorCategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteosPorCategoria: StateFlow<Map<String, Int>> = _conteosPorCategoria.asStateFlow()

    private val _pendientesSincronizar = MutableStateFlow(0)
    val pendientesSincronizar: StateFlow<Int> = _pendientesSincronizar.asStateFlow()

    private val _mensajeNotificacion = MutableStateFlow<String?>(null)
    val mensajeNotificacion: StateFlow<String?> = _mensajeNotificacion.asStateFlow()

    init {
        observarEstadoDeRed()
        cargarVocabulario()
        actualizarConteos()
    }

    private fun observarEstadoDeRed() {
        viewModelScope.launch {
            redUtilidad.observarConexion().collect { conectada ->
                _hayConexion.value = conectada
                if (conectada) {
                    // Al recuperar conexión a internet, sincronizar automáticamente con Firebase
                    sincronizarConNube()
                }
            }
        }
    }

    /**
     * Carga las palabras según la categoría actual y el término de búsqueda.
     */
    fun cargarVocabulario() {
        viewModelScope.launch {
            _estaCargando.value = true
            val lista = if (_terminoBusqueda.value.isBlank()) {
                repositorio.obtenerPalabras(_categoriaSeleccionada.value)
            } else {
                repositorio.buscarPalabras(_terminoBusqueda.value, _categoriaSeleccionada.value)
            }
            _palabras.value = lista
            _estaCargando.value = false
            actualizarConteos()
        }
    }

    /**
     * Cambia la categoría de filtro y recarga los datos.
     */
    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
        cargarVocabulario()
    }

    /**
     * Actualiza el filtro de búsqueda.
     */
    fun actualizarBusqueda(termino: String) {
        _terminoBusqueda.value = termino
        cargarVocabulario()
    }

    /**
     * Guarda una palabra (creación o actualización) en el cuaderno.
     */
    fun guardarPalabra(
        idExistente: String?,
        textoIngles: String,
        pronunciacionFonetica: String,
        traduccion: String,
        categoria: String,
        ejemplo: String,
        alCompletar: () -> Unit
    ) {
        if (textoIngles.isBlank() || traduccion.isBlank()) {
            _mensajeNotificacion.value = "Por favor completa el texto en inglés y la traducción"
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            val palabra = PalabraModelo(
                id = idExistente ?: java.util.UUID.randomUUID().toString(),
                textoIngles = textoIngles.trim(),
                pronunciacionFonetica = pronunciacionFonetica.trim(),
                traduccion = traduccion.trim(),
                categoria = categoria,
                ejemplo = ejemplo.trim(),
                fechaCreacion = System.currentTimeMillis()
            )

            repositorio.guardarPalabra(palabra)
            _mensajeNotificacion.value = "Palabra guardada en tu cuaderno"
            _estaCargando.value = false
            cargarVocabulario()
            alCompletar()
        }
    }

    /**
     * Elimina una palabra por su identificador.
     */
    fun eliminarPalabra(id: String) {
        viewModelScope.launch {
            repositorio.eliminarPalabra(id)
            _mensajeNotificacion.value = "Palabra eliminada del cuaderno"
            cargarVocabulario()
        }
    }

    /**
     * Ejecuta la sincronización bidireccional SQLite <-> Firebase.
     */
    fun sincronizarConNube() {
        viewModelScope.launch {
            if (_estaSincronizando.value) return@launch
            _estaSincronizando.value = true

            val exito = repositorio.sincronizarConFirebase()
            if (exito) {
                _mensajeNotificacion.value = "Sincronización con Firebase completada"
            }
            _estaSincronizando.value = false
            cargarVocabulario()
            actualizarConteos()
        }
    }

    /**
     * Reproduce la pronunciación en inglés de manera nativa y sin APIs externas.
     */
    fun pronunciarEnIngles(texto: String) {
        vozUtilidad.pronunciarEnIngles(texto)
    }

    /**
     * Actualiza el conteo de palabras por cada categoría y los pendientes de sincronizar.
     */
    private fun actualizarConteos() {
        viewModelScope.launch {
            val mapa = mutableMapOf<String, Int>()
            mapa[CategoriaModelo.TODOS.identificador] = repositorio.contarPorCategoria(CategoriaModelo.TODOS.identificador)
            mapa[CategoriaModelo.VERBOS.identificador] = repositorio.contarPorCategoria(CategoriaModelo.VERBOS.identificador)
            mapa[CategoriaModelo.ADJETIVOS.identificador] = repositorio.contarPorCategoria(CategoriaModelo.ADJETIVOS.identificador)
            mapa[CategoriaModelo.PRONOMBRES.identificador] = repositorio.contarPorCategoria(CategoriaModelo.PRONOMBRES.identificador)
            mapa[CategoriaModelo.PRONOMBRES_PERSONALES.identificador] = repositorio.contarPorCategoria(CategoriaModelo.PRONOMBRES_PERSONALES.identificador)
            mapa[CategoriaModelo.OTRA.identificador] = repositorio.contarPorCategoria(CategoriaModelo.OTRA.identificador)
            _conteosPorCategoria.value = mapa

            _pendientesSincronizar.value = repositorio.contarPendientesSincronizacion()
        }
    }

    /**
     * Refresca todos los datos del cuaderno cuando un usuario inicia o cierra sesión.
     */
    fun refrescarDatosUsuario() {
        cargarVocabulario()
        actualizarConteos()
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
                    val dao = PalabraDao(dbHelper)
                    val firebaseServicio = FirebaseServicio()
                    val redUtilidad = RedUtilidad(aplicacion)
                    val vozUtilidad = VozUtilidad(aplicacion)
                    val repositorio = RepositorioVocabulario(dao, firebaseServicio, redUtilidad)

                    return VocabularioViewModel(
                        aplicacion = aplicacion,
                        repositorio = repositorio,
                        redUtilidad = redUtilidad,
                        vozUtilidad = vozUtilidad
                    ) as T
                }
            }
        }
    }
}
