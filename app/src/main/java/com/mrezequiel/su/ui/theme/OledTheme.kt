package com.mrezequiel.su.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Tema OLED puro: fundo 100% preto para economizar bateria em tela AMOLED.
val DarkOledTheme = darkColorScheme(
    primary = Color(0xFFFFB86B),
    onPrimary = Color(0xFF4A2600),
    primaryContainer = Color(0xFF6B3A00),
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Color(0xFFDCC3A1),
    onSecondary = Color(0xFF3E2F16),
    background = Color(0xFF000000),
    onBackground = Color(0xFFECE1D3),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFECE1D3),
    surfaceVariant = Color(0xFF000000),
    onSurfaceVariant = Color(0xFFD3C4B4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)
