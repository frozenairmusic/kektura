package hu.kektura.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// OKT blue palette
val OktBlue = Color(0xFF1565C0)
val OktBlueLight = Color(0xFF5E92F3)
val OktBlueDark = Color(0xFF003C8F)
val OktBlueContainer = Color(0xFFD6E4FF)
val OktBlueOnContainer = Color(0xFF003C8F)

val StampCollected = Color(0xFF2E7D32)
val StampPending = Color(0xFF9E9E9E)

private val LightColorScheme = lightColorScheme(
    primary = OktBlue,
    onPrimary = Color.White,
    primaryContainer = OktBlueContainer,
    onPrimaryContainer = OktBlueOnContainer,
    secondary = OktBlueLight,
    secondaryContainer = Color(0xFFE3F0FF),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),
)

private val DarkColorScheme = darkColorScheme(
    primary = OktBlueLight,
    onPrimary = OktBlueDark,
    primaryContainer = OktBlue,
    onPrimaryContainer = OktBlueContainer,
    secondary = OktBlueLight,
    secondaryContainer = Color(0xFF1A3A5C),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFCAC4D0),
)

@Composable
fun KekturaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
