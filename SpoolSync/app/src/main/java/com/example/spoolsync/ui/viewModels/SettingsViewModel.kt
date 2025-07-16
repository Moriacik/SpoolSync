package com.example.spoolsync.ui.viewModels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val language = MutableStateFlow(prefs.getString("language", "en") ?: "en")


    fun setDarkMode(enabled: Boolean, activity: Activity? = null) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        activity?.recreate()
    }


    fun setLanguage(lang: String, activity: Activity? = null) {
        language.value = lang
        prefs.edit().putString("language", lang).apply()
        val locale = Locale(lang.lowercase())
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        getApplication<Application>().resources.updateConfiguration(
            config,
            getApplication<Application>().resources.displayMetrics
        )
        activity?.recreate()
    }
}