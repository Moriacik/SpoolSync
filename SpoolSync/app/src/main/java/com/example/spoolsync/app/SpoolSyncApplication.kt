package com.example.spoolsync.app

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.example.spoolsync.notification.Notification

/**
 * Aplikačná trieda pre SpoolSync.
 * Inicializuje Firebase a notifikačný kanál pri štarte aplikácie.
 */
class SpoolSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Notification.createNotificationChannel(this)
    }
}