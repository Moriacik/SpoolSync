package com.example.spoolsync.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class Filament(
    val id: String,
    val type: String,
    val brand: String,
    val weight: Int,
    val status: String,
    val color: Color,
    val expirationDate: LocalDate,
    val activeNfc: Boolean,
    val note: String
)