package ni.edu.uam.raccooncash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE1D5F9), // Morado claro
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A4458),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF90CAF9), // Azul claro
    onSecondary = Color.Black,
    background = Color(0xFF0F111A), // Fondo oscuro profundo
    onBackground = Color.White,
    surface = Color(0xFF1E222D), // Color de tarjetas
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C313F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5)
)

@Composable
fun RaccoonCashTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
