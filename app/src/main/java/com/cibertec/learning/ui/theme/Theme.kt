package com.cibertec.learning.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema Cálido Único y Fijo (inspirado en tonos trigo/arena, ámbar y terracota)
// Se mantiene consistente sin importar si el celular tiene activado el modo oscuro.
private val EsquemaCalido = lightColorScheme(
    primary = AmbarOscuroPrimario,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFED7AA),
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = CarameloSecundario,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = TerracotaOscuro,
    onTertiary = Color.White,
    background = ArenaFondoClaro,
    onBackground = TextoOscuroCalido,
    surface = ArenaSuperficie,
    onSurface = TextoOscuroCalido,
    surfaceVariant = ArenaSuperficieVariante,
    onSurfaceVariant = Color(0xFF57534E)
)

@Composable
fun LearningTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaCalido,
        typography = Typography,
        content = content
    )
}
