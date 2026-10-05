package com.mrezequiel.su.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import com.mrezequiel.su.APApplication
import com.mrezequiel.su.R
import java.security.MessageDigest

object AppLock {
    private const val KEY = "app_pin_hash"

    fun isSet(): Boolean =
        APApplication.sharedPreferences.getString(KEY, null) != null

    fun set(pin: String) {
        APApplication.sharedPreferences.edit {
            putString(KEY, sha(pin))
        }
    }

    fun remove() {
        APApplication.sharedPreferences.edit { remove(KEY) }
    }

    fun check(pin: String): Boolean {
        val saved = APApplication.sharedPreferences.getString(KEY, null) ?: return true
        return saved == sha(pin)
    }

    private fun sha(pin: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }
}

@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    if (!AppLock.isSet()) {
        content()
        return
    }
    var unlocked by remember { mutableStateOf(false) }
    if (unlocked) {
        content()
        return
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        PinPad(
            title = stringResource(id = R.string.app_lock_enter),
            onDone = { if (AppLock.check(it)) unlocked = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSettingRow() {
    var showDialog by remember { mutableStateOf(false) }
    var hasPin by remember { mutableStateOf(AppLock.isSet()) }

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.app_lock_title)) },
        supportingContent = {
            Text(
                text = stringResource(
                    id = if (hasPin) R.string.app_lock_on else R.string.app_lock_off
                )
            )
        },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        var confirmRemove by remember { mutableStateOf(false) }
        BasicAlertDialogWorkaround(
            onDismiss = { showDialog = false },
            content = {
                if (!hasPin) {
                    PinPad(
                        title = stringResource(id = R.string.app_lock_set),
                        onDone = {
                            AppLock.set(it)
                            hasPin = true
                            showDialog = false
                        }
                    )
                } else if (!confirmRemove) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(id = R.string.app_lock_title))
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(onClick = { confirmRemove = true }) {
                            Text(text = stringResource(id = R.string.app_lock_remove))
                        }
                    }
                } else {
                    PinPad(
                        title = stringResource(id = R.string.app_lock_enter),
                        onDone = {
                            AppLock.remove()
                            hasPin = false
                            showDialog = false
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun BasicAlertDialogWorkaround(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(310.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            content()
        }
    }
}

@Composable
fun PinPad(
    title: String,
    onDone: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "●".repeat(pin.length) + "○".repeat((4 - pin.length).coerceAtLeast(0)),
            fontSize = 32.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        if (error) {
            Text(
                text = stringResource(id = R.string.app_lock_wrong),
                color = MaterialTheme.colorScheme.error
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            items((1..9).toList() + listOf(-1, 0, -2)) { n ->
                when (n) {
                    -1 -> TextButton(onClick = { pin = ""; error = false }) {
                        Text(text = "C")
                    }
                    -2 -> TextButton(onClick = { pin = ""; error = false }) {
                        Text(text = "⌫")
                    }
                    else -> TextButton(onClick = {
                        error = false
                        if (pin.length < 4) pin += n.toString()
                        if (pin.length == 4) {
                            if (AppLock.check(pin)) {
                                onDone(pin)
                            } else {
                                error = true
                            }
                            pin = ""
                        }
                    }) {
                        Text(text = n.toString(), fontSize = 24.sp)
                    }
                }
            }
        }
        Text(
            text = stringResource(id = R.string.app_lock_hint),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
