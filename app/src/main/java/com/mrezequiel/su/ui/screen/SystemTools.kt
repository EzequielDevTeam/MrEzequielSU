package com.mrezequiel.su.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrezequiel.su.R
import com.mrezequiel.su.util.rootShellForResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RebootRow() {
    var show by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.sys_reboot_title)) },
        leadingContent = { Icon(Icons.Filled.Refresh, null) },
        modifier = Modifier.clickable { show = true }
    )

    if (show) {
        val options = listOf(
            R.string.sys_reboot_system to "",
            R.string.sys_reboot_recovery to "recovery",
            R.string.sys_reboot_bootloader to "bootloader",
            R.string.sys_reboot_edl to "edl"
        )
        SystemOptionsDialog(
            onDismiss = { show = false },
            options = options
        ) { mode ->
            show = false
            scope.launch {
                withContext(Dispatchers.IO) {
                    rootShellForResult(if (mode.isEmpty()) "reboot" else "reboot $mode")
                }
            }
        }
    }
}

@Composable
fun SelinuxRow() {
    var permissive by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val out = try {
                rootShellForResult("getenforce").out.firstOrNull()?.trim()
            } catch (_: Exception) {
                null
            }
            permissive = out.equals("Permissive", ignoreCase = true)
            loaded = true
        }
    }

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.sys_selinux_title)) },
        leadingContent = { Icon(Icons.Filled.Security, null) },
        trailingContent = {
            Switch(
                checked = permissive,
                enabled = loaded,
                onCheckedChange = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            rootShellForResult(if (it) "setenforce 0" else "setenforce 1")
                            val out = try {
                                rootShellForResult("getenforce").out.firstOrNull()?.trim()
                            } catch (_: Exception) {
                                null
                            }
                            permissive = out.equals("Permissive", ignoreCase = true)
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun AvcLogRow() {
    var show by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ListItem(
        headlineContent = { Text(text = stringResource(id = R.string.sys_avc_title)) },
        leadingContent = { Icon(Icons.Filled.Description, null) },
        modifier = Modifier.clickable {
            show = true
            log = null
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    try {
                        val out = rootShellForResult("dmesg | grep -i avc | tail -n 100").out
                        if (out.isEmpty()) null else out.joinToString("\n")
                    } catch (_: Exception) {
                        null
                    }
                }
                log = text
            }
        }
    )

    if (show) {
        SystemOptionsDialog(
            onDismiss = { show = false },
            options = emptyList(),
            customContent = {
                if (log == null) {
                    Text(text = stringResource(id = R.string.sys_avc_empty))
                } else {
                    Text(
                        text = log ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        ) { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemOptionsDialog(
    onDismiss: () -> Unit,
    options: List<Pair<Int, String>>,
    customContent: @Composable (() -> Unit)? = null,
    onPick: (String) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (customContent != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        customContent()
                    }
                } else {
                    LazyColumn {
                        items(options) { (labelId, value) ->
                            ListItem(
                                headlineContent = { Text(text = stringResource(id = labelId)) },
                                modifier = Modifier.clickable { onPick(value) }
                            )
                        }
                    }
                }
            }
        }
    }
}
