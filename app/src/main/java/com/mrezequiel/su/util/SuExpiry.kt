package com.mrezequiel.su.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.edit
import com.mrezequiel.su.APApplication
import com.mrezequiel.su.receiver.SuExpiryReceiver

// Root temporario: guarda quando a permissao expira e agenda a revogacao.
// "Sempre" (FOREVER) nunca revoga sozinho.
object SuExpiry {
    const val FOREVER = 0L
    const val ONE_HOUR = 3600_000L
    const val ONE_DAY = 86_400_000L
    const val SEVEN_DAYS = 604_800_000L

    private fun prefs() = APApplication.sharedPreferences
    private fun key(uid: Int) = "su_expiry_$uid"

    fun getExpiry(uid: Int): Long = prefs().getLong(key(uid), 0L)

    fun isTemporary(uid: Int): Boolean = getExpiry(uid) != 0L

    fun isExpired(uid: Int): Boolean {
        val e = getExpiry(uid)
        return e != 0L && System.currentTimeMillis() >= e
    }

    fun set(uid: Int, durationMs: Long, context: Context) {
        cancel(uid, context)
        if (durationMs <= 0L) return
        prefs().edit { putLong(key(uid), System.currentTimeMillis() + durationMs) }
        val intent = Intent(context, SuExpiryReceiver::class.java).putExtra("uid", uid)
        val pi = PendingIntent.getBroadcast(
            context, uid, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + durationMs, pi
        )
    }

    fun cancel(uid: Int, context: Context) {
        prefs().edit { remove(key(uid)) }
        val pi = PendingIntent.getBroadcast(
            context, uid, Intent(context, SuExpiryReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }
}
