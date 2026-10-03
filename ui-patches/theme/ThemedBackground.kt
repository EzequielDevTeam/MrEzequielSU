package com.mrezequiel.su.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import com.mrezequiel.su.APApplication

// Fundo personalizado global (cor solida, foto ou GIF).
// Configurado em Configuracoes > Fundo personalizado.
@Composable
fun ThemedBackground(content: @Composable () -> Unit) {
    val prefs = APApplication.sharedPreferences
    val refreshTick by refreshTheme.observeAsState(false)

    val mode = remember(refreshTick) { prefs.getString("bg_mode", "off") ?: "off" }
    val colorLong = remember(refreshTick) { prefs.getLong("bg_color", 0xFF1A120B) }
    val uri = remember(refreshTick) { prefs.getString("bg_uri", null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (mode) {
            "color" -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(colorLong.toULong()))
            )
            "photo" -> if (!uri.isNullOrEmpty()) {
                val context = LocalContext.current
                val loader = remember(uri) {
                    ImageLoader.Builder(context)
                        .components { add(GifDecoder.Factory()) }
                        .build()
                }
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    imageLoader = loader,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        content()
    }
}
