package com.example.spoolsync.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spoolsync.navigation.SpoolSyncApp
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.SettingsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private val nfcPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result is handled; NFC will work if granted
    }

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

        // Request NFC permission (required on Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            nfcPermissionLauncher.launch(android.Manifest.permission.NFC)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode = settingsViewModel.isDarkMode.collectAsState()

            // Firebase Auth check (has priority over SharedPreferences)
            val currentUser = Firebase.auth.currentUser
            val startDestination = when {
                currentUser == null -> "login"
                currentUser.isEmailVerified -> "filaments"
                else -> "verification"
            }

            SpoolSyncTheme(useDarkTheme = isDarkMode.value) {
                Surface {
                    SpoolSyncApp(startDestination)
                }
            }
        }
    }
}

