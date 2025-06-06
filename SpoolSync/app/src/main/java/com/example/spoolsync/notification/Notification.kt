package com.example.spoolsync.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object Notification {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "expiration_channel",
                "Filament Expiration Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for filament expiration"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, filamentId: String, filamentType: String, expirationDate: LocalDate) {
        val delay = expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()

        val workData = Data.Builder()
            .putString("filamentId", filamentId)
            .putString("filamentType", filamentType)
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)
    }
}