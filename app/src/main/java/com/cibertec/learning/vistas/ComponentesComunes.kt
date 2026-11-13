package com.cibertec.learning.vistas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibertec.learning.R
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.modelo.PalabraModelo

/**
 * Barra superior que muestra el estado de conexión y sincronización con Firebase y SQLite.
 */
@Composable
fun BarraEstadoConexion(
    hayConexion: Boolean,
    estaSincronizando: Boolean,
    pendientesSincronizar: Int,
    alSincronizarManual: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (hayConexion) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Indicador visual de punto de red
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (hayConexion) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = when {
                        estaSincronizando -> Icons.Default.CloudSync
                        hayConexion -> Icons.Default.CloudDone
                        else -> Icons.Default.CloudOff
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (hayConexion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = stringResource(
                            if (hayConexion) R.string.estado_en_linea else R.string.estado_sin_conexion
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (pendientesSincronizar > 0) {
                        Text(
                            text = "${stringResource(R.string.datos_pendientes_sincronizar)} ($pendientesSincronizar)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = hayConexion) {
                if (estaSincronizando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = alSincronizarManual,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.sincronizar_ahora),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual para presentar una palabra del cuaderno:
 * Cumple con el requisito:
 * - Texto en inglés con estilo BOLD.
 * - Abajo o al costado la traducción y pronunciación fonética SIN estilo BOLD.
 */
@Composable
fun TarjetaPalabra(
    palabra: PalabraModelo,
    alReproducirAudio: (String) -> Unit,
    alEditar: (PalabraModelo) -> Unit,
    alEliminar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    val categoriaModelo = CategoriaModelo.obtenerPorIdentificador(palabra.categoria)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Chip de categoría y estado de sincronización
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = stringResource(categoriaModelo.idTexto),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = categoriaModelo.icono,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null
                )

                // Etiqueta de sincronización SQLite / Firebase
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (palabra.sincronizado) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (palabra.sincronizado) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            if (palabra.sincronizado) R.string.etiqueta_sincronizado else R.string.etiqueta_pendiente
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // REQUISITO: Texto en inglés en BOLD
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = palabra.textoIngles,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, // ¡BOLD OBLIGATORIO SEGÚN REQUERIMIENTO!
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Botón de audio nativo (Text-to-Speech)
                IconButton(
                    onClick = { alReproducirAudio(palabra.textoIngles) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = stringResource(R.string.reproducir_audio),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // REQUISITO: Abajo o al costado pronunciación fonética SIN BOLD
            if (palabra.pronunciacionFonetica.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (palabra.pronunciacionFonetica.startsWith("[")) {
                        palabra.pronunciacionFonetica
                    } else {
                        "/${palabra.pronunciacionFonetica}/"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal, // ¡SIN BOLD SEGÚN REQUERIMIENTO!
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // REQUISITO: Traducción al español SIN BOLD
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = palabra.traduccion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal, // ¡SIN BOLD SEGÚN REQUERIMIENTO!
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Acciones: Editar y Eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { alEditar(palabra) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar_palabra),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { mostrarDialogoConfirmacion = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.eliminar),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text(text = stringResource(R.string.confirmar_eliminar_titulo)) },
            text = { Text(text = stringResource(R.string.confirmar_eliminar_mensaje)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        alEliminar(palabra.id)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text(text = stringResource(R.string.cancelar))
                }
            }
        )
    }
}

/**
 * Vista de estado vacío cuando el usuario aún no ha agregado palabras al cuaderno.
 */
@Composable
fun EstadoVacio(
    alAgregarPalabra: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.sin_palabras_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.sin_palabras_descripcion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        FilledTonalButton(
            onClick = alAgregarPalabra,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.agregar_palabra))
        }
    }
}
