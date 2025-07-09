package com.example.spoolsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.example.spoolsync.navigation.SpoolSyncApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val context = LocalContext.current
                    val sharedPref = context.getSharedPreferences("user_prefs", 0)
                    val userUid = sharedPref.getString("user_uid", null)
                    val startDestination = if (userUid != null) "filaments" else "login"
                    SpoolSyncApp(startDestination)
                }
            }
        }
    }
}