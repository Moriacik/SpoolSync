package com.example.spoolsync.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spoolsync.R

class NotificationReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val filamentId = intent.getStringExtra("filamentId") ?: return
        val filamentType = intent.getStringExtra("filamentType") ?: "Unknown"

        val notification = NotificationCompat.Builder(context, "expiration_channel")
            .setSmallIcon(R.drawable.ic_expiration)
            .setContentTitle("Filament Expiration Alert")
            .setContentText("Your filament $filamentType is about to expire!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(filamentId.hashCode(), notification)
    }
}