package com.example.spoolsync.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.spoolsync.R

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val filamentId = inputData.getString("filamentId") ?: return Result.failure()
        val filamentType = inputData.getString("filamentType") ?: "Unknown"

        val notification = NotificationCompat.Builder(applicationContext, "expiration_channel")
            .setSmallIcon(R.drawable.ic_expiration)
            .setContentTitle("Filament Expiration Alert")
            .setContentText("Your filament $filamentType is about to expire!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(filamentId.hashCode(), notification)

        return Result.success()
    }
}