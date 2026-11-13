package com.cibertec.learning.vistas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.cibertec.learning.R
import com.cibertec.learning.vistamodelo.OracionesViewModel
import com.cibertec.learning.vistamodelo.VocabularioViewModel

/**
 * Formulario para registrar una oración en inglés con su pronunciación fonética y traducción.
 *
 * Incluye recomendaciones predictivas en tiempo real mediante un menú autocompletado flotante
 * que sugiere palabras aprendidas (verbos, adjetivos, etc.) mientras el usuario teclea en inglés.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioOracion(
    idOracion: String?,
    oracionesViewModel: OracionesViewModel,
    vocabularioViewModel: VocabularioViewModel,
    alVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var campoInglesValor by remember { mutableStateOf(TextFieldValue("")) }
    var pronunciacionFonetica by remember { mutableStateOf("") }
    var traduccion by remember { mutableStateOf("") }
    var errorIngles by remember { mutableStateOf(false) }
    var errorTraduccion by remember { mutableStateOf(false) }
    var estaGuardando by remember { mutableStateOf(false) }

    // Palabras guardadas en el cuaderno por el usuario
    val todasLasPalabras by vocabularioViewModel.palabras.collectAsState()

    // Detectar la palabra actual que el usuario está escribiendo justo antes del cursor
    val textoHastaCursor = campoInglesValor.text.substring(
        0,
        campoInglesValor.selection.start.coerceIn(0, campoInglesValor.text.length)
    )
    val palabraActualEnTipeo = textoHastaCursor.takeLastWhile { it.isLetter() || it == '\'' }

    // Coincidencias predictivas en tiempo real mientras escribe
    val sugerenciasTipeo = remember(palabraActualEnTipeo, todasLasPalabras) {
        if (palabraActualEnTipeo.isNotBlank()) {
            todasLasPalabras.filter {
                it.textoIngles.startsWith(palabraActualEnTipeo, ignoreCase = true) &&
                        !it.textoIngles.equals(palabraActualEnTipeo, ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

    // Cargar datos en caso de edición
    LaunchedEffect(idOracion) {
        if (!idOracion.isNullOrBlank()) {
            val oracion = oracionesViewModel.oraciones.value.firstOrNull { it.id == idOracion }
            oracion?.let {
                campoInglesValor = TextFieldValue(it.textoIngles, selection = TextRange(it.textoIngles.length))
                pronunciacionFonetica = it.pronunciacionFonetica
                traduccion = it.traduccion
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (!idOracion.isNullOrBlank()) R.string.editar_oracion else R.string.agregar_oracion
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
                    // Campo 1: Oración en inglés con auto-completado en tiempo real
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = sugerenciasTipeo.isNotEmpty(),
                            onExpandedChange = { }
                        ) {
                            OutlinedTextField(
                                value = campoInglesValor,
                                onValueChange = {
                                    campoInglesValor = it
                                    if (errorIngles && it.text.isNotBlank()) errorIngles = false
                                },
                                label = { Text(text = stringResource(R.string.campo_oracion_ingles)) },
                                supportingText = { Text(text = stringResource(R.string.campo_oracion_ingles_ayuda)) },
                                isError = errorIngles,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                )
                            )

                            // Menú flotante de recomendaciones en vivo mientras escribe
                            ExposedDropdownMenu(
                                expanded = sugerenciasTipeo.isNotEmpty(),
                                onDismissRequest = { }
                            ) {
                                sugerenciasTipeo.forEach { palabra ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = palabra.textoIngles,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = palabra.traduccion,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            val textoActual = campoInglesValor.text
                                            val inicioCursor = campoInglesValor.selection.start
                                            val inicioPalabra = inicioCursor - palabraActualEnTipeo.length
                                            val nuevoTexto = textoActual.substring(0, inicioPalabra) +
                                                    palabra.textoIngles + " " +
                                                    textoActual.substring(inicioCursor)
                                            val nuevaPosicion = inicioPalabra + palabra.textoIngles.length + 1
                                            campoInglesValor = TextFieldValue(
                                                text = nuevoTexto,
                                                selection = TextRange(nuevaPosicion)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Campo 2: Pronunciación fonética (opcional)
                    OutlinedTextField(
                        value = pronunciacionFonetica,
                        onValueChange = { pronunciacionFonetica = it },
                        label = { Text(text = stringResource(R.string.campo_oracion_fonetica)) },
                        supportingText = { Text(text = stringResource(R.string.campo_oracion_fonetica_ayuda)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Campo 3: Traducción al español (obligatorio)
                    OutlinedTextField(
                        value = traduccion,
                        onValueChange = {
                            traduccion = it
                            if (errorTraduccion && it.isNotBlank()) errorTraduccion = false
                        },
                        label = { Text(text = stringResource(R.string.campo_oracion_traduccion)) },
                        supportingText = { Text(text = stringResource(R.string.campo_oracion_traduccion_ayuda)) },
                        isError = errorTraduccion,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            // Botones de acción
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
                        val inglesValido = campoInglesValor.text.isNotBlank()
                        val traduccionValida = traduccion.isNotBlank()
                        errorIngles = !inglesValido
                        errorTraduccion = !traduccionValida

                        if (inglesValido && traduccionValida) {
                            estaGuardando = true
                            oracionesViewModel.guardarOracion(
                                idExistente = idOracion,
                                textoIngles = campoInglesValor.text.trim(),
                                pronunciacionFonetica = pronunciacionFonetica,
                                traduccion = traduccion,
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
                        Text(text = stringResource(R.string.guardar_oracion))
                    }
                }
            }
        }
    }
}
