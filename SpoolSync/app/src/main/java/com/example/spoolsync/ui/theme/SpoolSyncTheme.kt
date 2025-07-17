package com.example.spoolsync.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun SpoolSyncTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    val spoolSyncColors = SpoolSyncColorsFactory.create(useDarkTheme)

    CompositionLocalProvider(LocalSpoolSyncColors provides spoolSyncColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

object SpoolSyncTheme {
    val colors: SpoolSyncColors
        @Composable
        get() = LocalSpoolSyncColors.current
}