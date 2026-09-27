package com.example.autotallerapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = AzulTaller,
    onPrimary = Color.White,
    primaryContainer = AzulTallerClaro,
    onPrimaryContainer = AzulTallerOscuro,
    secondary = GrisTaller,
    error = RojoError,
    background = Color(0xFFFBFBFD),
    surface = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF85B7EB),
    onPrimary = Color(0xFF042C53),
    primaryContainer = Color(0xFF0C447C),
    onPrimaryContainer = AzulTallerClaro,
    secondary = Color(0xFFB4B2A9),
    error = Color(0xFFF09595),
    background = Color(0xFF14161A),
    surface = Color(0xFF1B1E23)
)

@Composable
fun AutoTallerTheme(
    enOscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (enOscuro) EsquemaOscuro else EsquemaClaro
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista)
                .isAppearanceLightStatusBars = !enOscuro
        }
    }
    MaterialTheme(colorScheme = esquema, content = content)
}
