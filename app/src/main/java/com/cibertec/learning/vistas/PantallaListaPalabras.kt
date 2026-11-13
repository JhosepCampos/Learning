package com.cibertec.learning.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cibertec.learning.R
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.modelo.PalabraModelo
import com.cibertec.learning.vistamodelo.VocabularioViewModel

/**
 * Pantalla que lista el vocabulario del usuario, filtrado por categoría y con búsqueda.
 * Cumple con el requisito:
 * - Texto en inglés con estilo BOLD
 * - Pronunciación fonética y traducción abajo o al costado sin estilo BOLD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaPalabras(
    categoriaInicial: String,
    vocabularioViewModel: VocabularioViewModel,
    alVolver: () -> Unit,
    alNavegarAFormularioConCategoria: (String) -> Unit,
    alEditarPalabra: (PalabraModelo) -> Unit,
    modifier: Modifier = Modifier
) {
    val palabras by vocabularioViewModel.palabras.collectAsState()
    val categoriaSeleccionada by vocabularioViewModel.categoriaSeleccionada.collectAsState()
    val terminoBusqueda by vocabularioViewModel.terminoBusqueda.collectAsState()
    val estaCargando by vocabularioViewModel.estaCargando.collectAsState()
    val mensajeNotificacion by vocabularioViewModel.mensajeNotificacion.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(categoriaInicial) {
        vocabularioViewModel.seleccionarCategoria(categoriaInicial)
    }

    LaunchedEffect(mensajeNotificacion) {
        mensajeNotificacion?.let {
            snackbarHostState.showSnackbar(it)
            vocabularioViewModel.limpiarMensaje()
        }
    }

    val modeloActual = CategoriaModelo.obtenerPorIdentificador(categoriaSeleccionada)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(modeloActual.idTexto),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.total_registros, palabras.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.boton_volver)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { alNavegarAFormularioConCategoria(categoriaSeleccionada) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.agregar_palabra)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barra de búsqueda en el cuaderno
            OutlinedTextField(
                value = terminoBusqueda,
                onValueChange = { vocabularioViewModel.actualizarBusqueda(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text(text = stringResource(R.string.buscar_en_cuaderno)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                trailingIcon = {
                    if (terminoBusqueda.isNotBlank()) {
                        IconButton(onClick = { vocabularioViewModel.actualizarBusqueda("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Chips horizontales de categorías para filtrado rápido
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoriaModelo.entries.toTypedArray()) { categoria ->
                    val estaSeleccionado = categoria.identificador == categoriaSeleccionada
                    FilterChip(
                        selected = estaSeleccionado,
                        onClick = { vocabularioViewModel.seleccionarCategoria(categoria.identificador) },
                        label = { Text(text = stringResource(categoria.idTexto)) },
                        leadingIcon = {
                            Icon(
                                imageVector = categoria.icono,
                                contentDescription = null,
                                modifier = Modifier.height(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de palabras o Estado Vacío (inicialmente la app no tiene data)
            if (estaCargando) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (palabras.isEmpty()) {
                if (terminoBusqueda.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.sin_resultados_busqueda),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    // Estado vacío cuando el usuario aún no agrega datos
                    EstadoVacio(
                        alAgregarPalabra = { alNavegarAFormularioConCategoria(categoriaSeleccionada) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = palabras,
                        key = { it.id }
                    ) { palabra ->
                        TarjetaPalabra(
                            palabra = palabra,
                            alReproducirAudio = { texto -> vocabularioViewModel.pronunciarEnIngles(texto) },
                            alEditar = alEditarPalabra,
                            alEliminar = { id -> vocabularioViewModel.eliminarPalabra(id) }
                        )
                    }

                    // Espacio final para que el FAB no tape el último elemento
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}
