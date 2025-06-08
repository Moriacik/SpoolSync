package com.example.spoolsync.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.spoolsync.R
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Objekt zodpovedný za správu notifikácií v aplikácii.
 * Obsahuje metódy na vytvorenie notifikačného kanála a plánovanie notifikácií.
 */
@SuppressLint("StaticFieldLeak")
object Notification {

    /**
     * Vytvorí notifikačný kanál pre upozornenia na exspiráciu filamentu.
     *
     * @param context Kontext aplikácie.
     */
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

    /**
     * Naplánuje notifikáciu o exspirácii filamentu na základe dátumu.
     *
     * @param context Kontext aplikácie.
     * @param filamentId Identifikátor filamentu.
     * @param expirationDate Dátum exspirácie filamentu.
     */
    fun scheduleNotification(context: Context, filamentId: String, expirationDate: LocalDate) {
        val delay = expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()

        val workData = Data.Builder()
            .putString("filamentId", filamentId)
            .putString("filamentAlert", context.getString(R.string.filament_alert))
            .putString("filamentNotification", context.getString(R.string.filament_notification))
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)
    }
}