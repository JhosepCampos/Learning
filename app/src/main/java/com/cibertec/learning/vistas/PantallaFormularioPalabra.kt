package com.cibertec.learning.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cibertec.learning.R
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.vistamodelo.VocabularioViewModel

/**
 * Pantalla de registro del cuaderno: permite al usuario ingresar una nueva palabra
 * con su texto en inglés, pronunciación fonética, traducción, categoría y ejemplos.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFormularioPalabra(
    idPalabra: String?,
    categoriaPredeterminada: String?,
    vocabularioViewModel: VocabularioViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textoIngles by remember { mutableStateOf("") }
    var pronunciacionFonetica by remember { mutableStateOf("") }
    var traduccion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember {
        mutableStateOf(
            if (!categoriaPredeterminada.isNullOrBlank() && categoriaPredeterminada != CategoriaModelo.TODOS.identificador) {
                categoriaPredeterminada
            } else {
                CategoriaModelo.VERBOS.identificador
            }
        )
    }
    var errorIngles by remember { mutableStateOf(false) }
    var errorTraduccion by remember { mutableStateOf(false) }
    var estaGuardando by remember { mutableStateOf(false) }

    // Cargar datos si es edición
    LaunchedEffect(idPalabra) {
        if (!idPalabra.isNullOrBlank()) {
            val palabra = vocabularioViewModel.palabras.value.firstOrNull { it.id == idPalabra }
            palabra?.let {
                textoIngles = it.textoIngles
                pronunciacionFonetica = it.pronunciacionFonetica
                traduccion = it.traduccion
                categoriaSeleccionada = it.categoria
            }
        }
    }

    val esModoEdicion = !idPalabra.isNullOrBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (esModoEdicion) R.string.editar_palabra else R.string.agregar_palabra
                        ),
                        fontWeight = FontWeight.Bold
                    )
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de entrada del cuaderno
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Campo 1: Texto en Inglés (Requerido)
                    OutlinedTextField(
                        value = textoIngles,
                        onValueChange = {
                            textoIngles = it
                            if (errorIngles && it.isNotBlank()) errorIngles = false
                        },
                        label = { Text(text = stringResource(R.string.campo_texto_ingles)) },
                        supportingText = { Text(text = stringResource(R.string.campo_texto_ingles_ayuda)) },
                        isError = errorIngles,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Campo 2: Pronunciación Fonética (Opcional)
                    OutlinedTextField(
                        value = pronunciacionFonetica,
                        onValueChange = { pronunciacionFonetica = it },
                        label = { Text(text = stringResource(R.string.campo_pronunciacion_fonetica)) },
                        supportingText = { Text(text = stringResource(R.string.campo_pronunciacion_fonetica_ayuda)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Campo 3: Traducción al Español (Requerido)
                    OutlinedTextField(
                        value = traduccion,
                        onValueChange = {
                            traduccion = it
                            if (errorTraduccion && it.isNotBlank()) errorTraduccion = false
                        },
                        label = { Text(text = stringResource(R.string.campo_traduccion)) },
                        supportingText = { Text(text = stringResource(R.string.campo_traduccion_ayuda)) },
                        isError = errorTraduccion,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        )
                    )

                    // Campo 4: Categoría de aprendizaje
                    Text(
                        text = stringResource(R.string.campo_categoria),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaModelo.obtenerCategoriasDisponiblesParaRegistro().forEach { categoria ->
                            val estaSeleccionado = categoria.identificador == categoriaSeleccionada
                            FilterChip(
                                selected = estaSeleccionado,
                                onClick = { categoriaSeleccionada = categoria.identificador },
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
                }
            }

            // Botones de acción (Guardar / Cancelar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = alVolver,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.cancelar))
                }

                Button(
                    onClick = {
                        val inglesValido = textoIngles.isNotBlank()
                        val traduccionValida = traduccion.isNotBlank()

                        errorIngles = !inglesValido
                        errorTraduccion = !traduccionValida

                        if (inglesValido && traduccionValida) {
                            estaGuardando = true
                            vocabularioViewModel.guardarPalabra(
                                idExistente = idPalabra,
                                textoIngles = textoIngles,
                                pronunciacionFonetica = pronunciacionFonetica,
                                traduccion = traduccion,
                                categoria = categoriaSeleccionada,
                                ejemplo = "",
                                alCompletar = {
                                    estaGuardando = false
                                    alVolver()
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !estaGuardando
                ) {
                    if (estaGuardando) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.guardar))
                    }
                }
            }
        }
    }
}
