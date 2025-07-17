package com.example.spoolsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.spoolsync.R
import com.google.firebase.annotations.concurrent.Background

@Immutable
data class SpoolSyncColors(
    val navBar: Color,
    val navBarIcon: Color,
    val navBarIconSelected: Color,
    val divider: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val filamentCircleBorder: Color,
    val filamentPhotoBorder: Color,
    val filamentPhotoBackground: Color,
    val filamentPhotoIcon: Color,
    val noteBox: Color,
    val buttonBackground: Color,
    val textBoxBackground: Color
)

object SpoolSyncColorsFactory {
    @Composable
    fun create(isDark: Boolean = isSystemInDarkTheme()): SpoolSyncColors = if (isDark) {
        SpoolSyncColors(
            navBar = colorResource(R.color.darker_gray),
            navBarIcon = colorResource(R.color.gray),
            navBarIconSelected = colorResource(R.color.white),
            divider = colorResource(R.color.dark_gray),
            chipBackground = colorResource(R.color.darker_gray),
            chipBorder = colorResource(R.color.gray),
            filamentCircleBorder = colorResource(R.color.gray),
            filamentPhotoBorder = colorResource(R.color.dark_gray),
            filamentPhotoBackground = colorResource(R.color.darker_gray),
            filamentPhotoIcon = colorResource(R.color.white),
            noteBox = colorResource(R.color.gray),
            buttonBackground = colorResource(R.color.gray),
            textBoxBackground = colorResource(R.color.gray)
        )
    } else {
        // Light theme colors
        SpoolSyncColors(
            navBar = colorResource(R.color.nav_bar_light),
            navBarIcon = colorResource(R.color.gray),
            navBarIconSelected = colorResource(R.color.black),
            divider = colorResource(R.color.light_gray),
            chipBackground = colorResource(R.color.light_gray),
            chipBorder = colorResource(R.color.dark_gray),
            filamentCircleBorder = colorResource(R.color.dark_gray),
            filamentPhotoBorder = colorResource(R.color.light_gray),
            filamentPhotoBackground = colorResource(R.color.white),
            filamentPhotoIcon = colorResource(R.color.black),
            noteBox = colorResource(R.color.light_gray),
            buttonBackground = colorResource(R.color.light_gray),
            textBoxBackground = colorResource(R.color.light_gray)
        )
    }
}


val LocalSpoolSyncColors = staticCompositionLocalOf {
    SpoolSyncColors(
        navBar = Color.Unspecified,
        navBarIcon = Color.Unspecified,
        navBarIconSelected = Color.Unspecified,
        divider = Color.Unspecified,
        chipBackground = Color.Unspecified,
        chipBorder = Color.Unspecified,
        filamentCircleBorder = Color.Unspecified,
        filamentPhotoBorder = Color.Unspecified,
        filamentPhotoBackground = Color.Unspecified,
        filamentPhotoIcon = Color.Unspecified,
        noteBox = Color.Unspecified,
        buttonBackground = Color.Unspecified,
        textBoxBackground = Color.Unspecified
    )
}