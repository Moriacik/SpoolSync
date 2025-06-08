package com.example.spoolsync.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * Dátová trieda reprezentujúca jeden filament.
 *
 * @property id Jedinečný identifikátor filamentu.
 * @property type Typ filamentu.
 * @property brand Značka výrobcu filamentu.
 * @property weight Hmotnosť filamentu v gramoch.
 * @property status Stav filamentu.
 * @property color Farba filamentu.
 * @property expirationDate Dátum exspirácie filamentu.
 * @property activeNfc Príznak, či je NFC aktívne.
 * @property note Poznámka k filamentu.
 */
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