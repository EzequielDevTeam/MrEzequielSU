package com.mrezequiel.su.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mrezequiel.su.Natives
import com.mrezequiel.su.util.SuExpiry

// Dispara quando um root temporario vence: revoga e limpa.
class SuExpiryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uid = intent.getIntExtra("uid", -1)
        if (uid < 0 || !SuExpiry.isExpired(uid)) return
        try {
            Natives.revokeSu(uid)
        } catch (_: Exception) {
        }
        SuExpiry.cancel(uid, context)
    }
}
