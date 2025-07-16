package com.example.spoolsync.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun SpoolSyncTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) {
        darkColorScheme(
        )
    } else {
        lightColorScheme(
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}