package com.example.spoolsync.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spoolsync.navigation.SpoolSyncApp
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("user_prefs", 0)
        val lang = sharedPref.getString("language", "en") ?: "en"
        val locale = java.util.Locale(lang)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode = settingsViewModel.isDarkMode.collectAsState()
            val sharedPref = this.getSharedPreferences("user_prefs", 0)
            val userUid = sharedPref.getString("user_uid", null)
            val startDestination = if (userUid != null) "filaments" else "login"
            SpoolSyncTheme(useDarkTheme = isDarkMode.value) {
                Surface {
                    SpoolSyncApp(startDestination)
                }
            }
        }
    }
}