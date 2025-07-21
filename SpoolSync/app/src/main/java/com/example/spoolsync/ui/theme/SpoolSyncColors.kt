package com.example.spoolsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.spoolsync.R

@Immutable
data class SpoolSyncColors(
    val lighterGrayDarkerGray: Color,
    val blackWhite: Color,
    val lightGrayDarkGray: Color,
    val lightGrayDarkerGray: Color,
    val darkGrayGray: Color,
    val whiteDarkerGray: Color,
    val lightGrayWhite: Color,
    val lightGrayGray: Color,
    val whiteGray: Color
)

object SpoolSyncColorsFactory {
    @Composable
    fun create(isDark: Boolean = isSystemInDarkTheme()): SpoolSyncColors = if (isDark) {
        SpoolSyncColors(
            lighterGrayDarkerGray = colorResource(R.color.darker_gray),
            blackWhite = colorResource(R.color.white),
            lightGrayDarkGray = colorResource(R.color.dark_gray),
            lightGrayDarkerGray = colorResource(R.color.darker_gray),
            darkGrayGray = colorResource(R.color.gray),
            whiteDarkerGray = colorResource(R.color.darker_gray),
            lightGrayWhite = colorResource(R.color.white),
            lightGrayGray = colorResource(R.color.gray),
            whiteGray = colorResource(R.color.gray)
        )
    } else {
        // Light theme colors
        SpoolSyncColors(
            lighterGrayDarkerGray = colorResource(R.color.lighter_gray),
            blackWhite = colorResource(R.color.black),
            lightGrayDarkGray = colorResource(R.color.light_gray),
            lightGrayDarkerGray = colorResource(R.color.light_gray),
            darkGrayGray = colorResource(R.color.dark_gray),
            whiteDarkerGray = colorResource(R.color.white),
            lightGrayWhite = colorResource(R.color.light_gray),
            lightGrayGray = colorResource(R.color.light_gray),
            whiteGray = colorResource(R.color.white)
        )
    }
}


val LocalSpoolSyncColors = staticCompositionLocalOf {
    SpoolSyncColors(
        lighterGrayDarkerGray = Color.Unspecified,
        blackWhite = Color.Unspecified,
        lightGrayDarkGray = Color.Unspecified,
        lightGrayDarkerGray = Color.Unspecified,
        darkGrayGray = Color.Unspecified,
        whiteDarkerGray = Color.Unspecified,
        lightGrayWhite = Color.Unspecified,
        lightGrayGray = Color.Unspecified,
        whiteGray = Color.Unspecified
    )
}