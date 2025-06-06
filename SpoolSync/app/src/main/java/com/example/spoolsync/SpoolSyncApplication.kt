package com.example.spoolsync

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.example.spoolsync.notification.Notification

class SpoolSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Notification.createNotificationChannel(this)
    }
}