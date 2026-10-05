package com.mrezequiel.su.util

import androidx.core.content.edit
import com.mrezequiel.su.APApplication
import com.topjohnwu.superuser.Shell
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Backup automatico do boot antes de cada patch + historico.
// Guarda em /data/adb/mre_backups (mantem os 5 mais recentes).
object BootBackup {
    private const val DIR = "/data/adb/mre_backups"
    private const val KEY_HISTORY = "boot_history"

    data class Entry(val path: String, val date: String, val slot: String)

    fun list(): List<Entry> {
        val out = mutableListOf<Entry>()
        try {
            val arr = JSONArray(
                APApplication.sharedPreferences.getString(KEY_HISTORY, "[]") ?: "[]"
            )
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Entry(o.getString("path"), o.getString("date"), o.getString("slot")))
            }
        } catch (_: Exception) {
        }
        return out
    }

    private fun save(entries: List<Entry>) {
        val arr = JSONArray()
        entries.forEach {
            arr.put(JSONObject().put("path", it.path).put("date", it.date).put("slot", it.slot))
        }
        APApplication.sharedPreferences.edit { putString(KEY_HISTORY, arr.toString()) }
    }

    // Retorna o caminho do backup ou null se falhou. Roda com root.
    fun backup(tag: String = "pre-patch"): String? {
        return try {
            val slot = try {
                Shell.cmd("getprop ro.boot.slot_suffix").exec().out.firstOrNull()
                    ?.trim().orEmpty()
            } catch (_: Exception) {
                ""
            }
            val part = if (slot.isEmpty()) "boot" else "boot$slot"
            val date = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            val dest = "$DIR/boot-$part-$date.img"
            val res = Shell.cmd(
                "mkdir -p $DIR",
                "dd if=/dev/block/by-name/$part of=$dest bs=4M",
                "ls -lh $dest"
            ).exec()
            if (!res.isSuccess) return null
            val entries = (listOf(Entry(dest, date, part)) + list()) .take(5)
            // apaga excedentes
            list().drop(5).forEach {
                try {
                    Shell.cmd("rm -f ${it.path}").exec()
                } catch (_: Exception) {
                }
            }
            save(entries)
            dest
        } catch (_: Exception) {
            null
        }
    }
}
