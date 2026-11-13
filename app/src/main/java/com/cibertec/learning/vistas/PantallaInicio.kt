package com.cibertec.learning.vistas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cibertec.learning.R
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.vistamodelo.AutenticacionViewModel
import com.cibertec.learning.vistamodelo.VocabularioViewModel

/**
 * Pantalla principal con diseño de tarjetas cuadradas en cuadrícula de 2 columnas (ícono arriba, nombre abajo)
 * para que toda la información quede a la vista del usuario con colores cálidos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    vocabularioViewModel: VocabularioViewModel,
    autenticacionViewModel: AutenticacionViewModel,
    alNavegarACategoria: (String) -> Unit,
    alNavegarAFormulario: () -> Unit,
    alNavegarAOraciones: () -> Unit,
    alNavegarALogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hayConexion by vocabularioViewModel.hayConexion.collectAsState()
    val estaSincronizando by vocabularioViewModel.estaSincronizando.collectAsState()
    val conteos by vocabularioViewModel.conteosPorCategoria.collectAsState()
    val pendientesSincronizar by vocabularioViewModel.pendientesSincronizar.collectAsState()
    val mensajeNotificacion by vocabularioViewModel.mensajeNotificacion.collectAsState()
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensajeNotificacion) {
        mensajeNotificacion?.let {
            snackbarHostState.showSnackbar(it)
            vocabularioViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.titulo_inicio),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (usuarioActual != null && usuarioActual!!.uid.isNotBlank()) {
                                stringResource(R.string.usuario_conectado, usuarioActual!!.correo)
                            } else {
                                stringResource(R.string.usuario_anonimo)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = alNavegarALogin) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.menu_perfil),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = alNavegarAFormulario,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Estado de conexión SQLite / Firebase
            item {
                BarraEstadoConexion(
                    hayConexion = hayConexion,
                    estaSincronizando = estaSincronizando,
                    pendientesSincronizar = pendientesSincronizar,
                    alSincronizarManual = { vocabularioViewModel.sincronizarConNube() }
                )
            }

            // Resumen de progreso del cuaderno
            item {
                val totalPalabras = conteos[CategoriaModelo.TODOS.identificador] ?: 0
                TarjetaProgresoCuaderno(
                    totalPalabras = totalPalabras,
                    alVerTodo = { alNavegarACategoria(CategoriaModelo.TODOS.identificador) }
                )
            }

            // Encabezado de categorías
            item {
                Text(
                    text = stringResource(R.string.seccion_categorias),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Cuadrícula 2x2 para Verbos y Adjetivos (Tarjetas cuadradas)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaCuadradaCategoria(
                        titulo = stringResource(R.string.categoria_verbos),
                        icono = Icons.Default.Bolt,
                        cantidad = conteos[CategoriaModelo.VERBOS.identificador] ?: 0,
                        alHacerClic = { alNavegarACategoria(CategoriaModelo.VERBOS.identificador) },
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaCuadradaCategoria(
                        titulo = stringResource(R.string.categoria_adjetivos),
                        icono = Icons.Default.AutoAwesome,
                        cantidad = conteos[CategoriaModelo.ADJETIVOS.identificador] ?: 0,
                        alHacerClic = { alNavegarACategoria(CategoriaModelo.ADJETIVOS.identificador) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Cuadrícula 2x2 para Pronombres y Pronombres Personales (Tarjetas cuadradas)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaCuadradaCategoria(
                        titulo = stringResource(R.string.categoria_pronombres),
                        icono = Icons.Default.Psychology,
                        cantidad = conteos[CategoriaModelo.PRONOMBRES.identificador] ?: 0,
                        alHacerClic = { alNavegarACategoria(CategoriaModelo.PRONOMBRES.identificador) },
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaCuadradaCategoria(
                        titulo = stringResource(R.string.categoria_pronombres_personales),
                        icono = Icons.Default.Person,
                        cantidad = conteos[CategoriaModelo.PRONOMBRES_PERSONALES.identificador] ?: 0,
                        alHacerClic = { alNavegarACategoria(CategoriaModelo.PRONOMBRES_PERSONALES.identificador) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Separador de sección: Comunicación rápida
            item {
                Text(
                    text = stringResource(R.string.seccion_comunicacion),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Tarjeta destacada de Oraciones
            item {
                TarjetaCuadradaOraciones(
                    titulo = stringResource(R.string.categoria_oraciones),
                    descripcion = stringResource(R.string.desc_oraciones),
                    icono = Icons.Default.Chat,
                    alHacerClic = alNavegarAOraciones
                )
            }

            // Espacio final para que el FAB no tape el contenido
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

/**
 * Tarjeta Cuadrada para cada categoría de vocabulario (Verbos, Adjetivos, etc.)
 * Muestra el ícono arriba, el nombre abajo y una insignia con la cantidad de palabras.
 */
@Composable
private fun TarjetaCuadradaCategoria(
    titulo: String,
    icono: ImageVector,
    cantidad: Int,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { alHacerClic() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Insignia con el conteo en la esquina superior derecha
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = cantidad.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Spacer(modifier = Modifier.size(8.dp))
            }

            // Contenido central: Ícono arriba, Nombre abajo
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Tarjeta rectangular horizontal para la sección de Mis Oraciones.
 */
@Composable
private fun TarjetaCuadradaOraciones(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    alHacerClic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { alHacerClic() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tarjeta superior de progreso en tono cálido.
 */
@Composable
private fun TarjetaProgresoCuaderno(
    totalPalabras: Int,
    alVerTodo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { alVerTodo() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.resumen_progreso),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.total_registros, totalPalabras),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.categoria_todos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
