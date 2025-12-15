package com.example.spoolsync.data.model

import android.util.Log
import androidx.compose.ui.graphics.Color
import java.io.Serializable

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
 * @property ownerId Identifikátor vlastníka filamentu.
 * @property originalWeight Pôvodná hmotnosť filamentu v gramoch.
 */

data class Filament(
    val id: String = "",
    val type: String = "",
    val brand: String = "",
    val weight: Int = 0,
    val status: String = "",
    val colorHex: String = "#FFFFFFFF",
    val expirationDate: String = "",
    val activeNfc: Boolean = false,
    val note: String = "",
    val ownerId: String = "",
    val originalWeight: Int = 0
) : Serializable {
    val color: Color
        get() = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.White
        }

    companion object {
        fun fromMap(data: Map<String, Any>): Filament {
            return try {
                val filament = Filament(
                    id = data["id"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    brand = data["brand"] as? String ?: "",
                    weight = (data["weight"] as? Number)?.toInt() ?: 0,
                    status = data["status"] as? String ?: "",
                    colorHex = data["color"] as? String ?: "#FFFFFFFF",
                    expirationDate = data["expirationDate"] as? String ?: "",
                    activeNfc = data["activeNfc"] as? Boolean ?: false,
                    note = data["note"] as? String ?: "",
                    ownerId = data["ownerId"] as? String ?: "",
                    originalWeight = (data["originalWeight"] as? Number)?.toInt() ?: 0
                )
                filament
            } catch (e: Exception) {
                throw e
            }
        }
    }
}