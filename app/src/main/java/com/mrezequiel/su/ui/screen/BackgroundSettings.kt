package com.mrezequiel.su.ui.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.edit
import com.mrezequiel.su.APApplication
import com.mrezequiel.su.R
import com.mrezequiel.su.ui.theme.refreshTheme
import com.mrezequiel.su.util.ui.APDialogBlurBehindUtils

private val bgPresetColors = listOf(
    0xFF8A4A00, 0xFF1A120B, 0xFF0B3D2E,
    0xFF1A237E, 0xFF4A148C, 0xFF000000
)

private fun bgModeLabel(mode: String): Int = when (mode) {
    "color" -> R.string.bg_color
    "photo" -> R.string.bg_photo
    else -> R.string.bg_off
}

@Composable
fun BackgroundSettingRow() {
    val context = LocalContext.current
    val prefs = APApplication.sharedPreferences
    val showDialog = remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(prefs.getString("bg_mode", "off") ?: "off") }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            prefs.edit {
                putString("bg_mode", "photo")
                putString("bg_uri", uri.toString())
            }
            mode = "photo"
            refreshTheme.value = true
        }
    }

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.bg_title)) },
        supportingContent = { Text(text = stringResource(id = bgModeLabel(mode))) },
        leadingContent = { Icon(Icons.Filled.Wallpaper, null) },
        modifier = Modifier.clickable { showDialog.value = true }
    )

    if (showDialog.value) {
        BackgroundChooseDialog(
            showDialog = showDialog,
            onModeChange = { mode = it },
            onPick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundChooseDialog(
    showDialog: MutableState<Boolean>,
    onModeChange: (String) -> Unit,
    onPick: () -> Unit
) {
    val prefs = APApplication.sharedPreferences
    var mode by remember { mutableStateOf(prefs.getString("bg_mode", "off") ?: "off") }

    fun selectMode(value: String) {
        prefs.edit { putString("bg_mode", value) }
        mode = value
        onModeChange(value)
        refreshTheme.value = true
    }

    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier.size(310.dp, 400.dp),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = stringResource(id = R.string.bg_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                val options = listOf("off", "color", "photo")
                val labels = listOf(R.string.bg_off, R.string.bg_color, R.string.bg_photo)
                options.forEachIndexed { index, value ->
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (value == "photo") {
                                        onPick()
                                        showDialog.value = false
                                    } else {
                                        selectMode(value)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = mode == value,
                                onClick = null
                            )
                            Text(
                                text = stringResource(id = labels[index]),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(id = R.string.bg_color),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bgPresetColors.forEach { argb ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(argb.toInt()))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .clickable {
                                        prefs.edit { putLong("bg_color", argb) }
                                        selectMode("color")
                                    }
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            onPick()
                            showDialog.value = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(text = stringResource(id = R.string.bg_pick))
                    }
                }
            }

            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}
