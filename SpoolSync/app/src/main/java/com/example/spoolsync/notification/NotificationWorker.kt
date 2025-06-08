package com.example.spoolsync.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spoolsync.R

/**
 * Worker zodpovedný za zobrazovanie notifikácií o exspirácii filamentu.
 * Spúšťa sa na základe naplánovanej úlohy a zobrazí upozornenie používateľovi.
 *
 * @param context Kontext aplikácie, v ktorom sa worker spúšťa.
 * @param workerParams Parametre pre worker, vrátane vstupných dát.
 */
class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    /**
     * Zobrazí notifikáciu o exspirácii filamentu.
     *
     * @return Výsledok vykonania úlohy.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val filamentId = inputData.getString("filamentId") ?: return Result.failure()
        val filamentAlert = inputData.getString("filamentAlert") ?: ""
        val filamentNotification = inputData.getString("filamentNotification") ?: ""

        val notification = NotificationCompat.Builder(applicationContext, "expiration_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(filamentAlert)
            .setContentText(filamentNotification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(filamentId.hashCode(), notification)

        return Result.success()
    }
}