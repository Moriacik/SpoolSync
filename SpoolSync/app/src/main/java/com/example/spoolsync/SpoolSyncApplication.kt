package com.example.spoolsync

import android.app.Application
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class SpoolSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Firebase.initialize(this)
            Log.d("SpoolSyncApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("SpoolSyncApp", "Firebase initialization failed", e)
        }
    }
}