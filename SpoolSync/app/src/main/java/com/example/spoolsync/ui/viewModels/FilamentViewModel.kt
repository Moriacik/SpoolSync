package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import com.example.spoolsync.data.model.Filament
import com.example.spoolsync.notification.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.UUID

/**
 * ViewModel pre správu filamentov používateľa.
 * Zodpovedá za načítanie, pridávanie, aktualizáciu a mazanie filamentov v databáze Firestore.
 * Umožňuje tiež aktualizovať hmotnosť a NFC stav filamentu, načítať filament podľa ID a spravovať notifikácie.
 *
 * @param application Kontext aplikácie potrebný pre AndroidViewModel.
 */
class FilamentViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val db: FirebaseFirestore = Firebase.firestore
    private var userId = Firebase.auth.currentUser?.uid ?: ""
    val currentFilament = mutableStateOf<Filament?>(null)
    val filaments = mutableStateListOf<Filament>()

    /**
     * Inicializuje ViewModel a načíta filamenty používateľa.
     * Ak nie je používateľ prihlásený, nastaví userId na prázdny reťazec.
     */
    fun setUserId(newUserId: String) {
        userId = newUserId
        loadFilaments()
    }

    /**
     * Načíta všetky filamenty používateľa z Firestore a aktualizuje stav filaments.
     * Používa addSnapshotListener pre sledovanie zmien v reálnom čase.
     */
    fun loadFilaments() {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                filaments.clear()
                val userFilaments = snapshot?.get("filaments") as? List<Map<String, Any>>
                userFilaments?.forEach { data ->
                    try {
                        filaments.add(
                            Filament(
                                id = data["id"] as? String ?: "",
                                type = data["type"] as? String ?: "",
                                brand = data["brand"] as? String ?: "",
                                weight = (data["weight"] as? Number)?.toInt() ?: 0,
                                status = data["status"] as? String ?: "",
                                color = Color(android.graphics.Color.parseColor(data["color"] as? String ?: "#000000")),
                                expirationDate = (data["expirationDate"] as? String).let { LocalDate.parse(it) },
                                activeNfc = (data["activeNfc"] as? Boolean) == false,
                                note = data["note"] as? String ?: ""
                            )
                        )
                    }
                    catch (_: Exception) {
                    }
                }
            }
    }

    /**
     * Uloží nový filament do Firestore a naplánuje notifikáciu pre jeho expiráciu.
     * @param filament Objekt Filament, ktorý sa má uložiť.
     */
    fun saveNewFilament(
        filament: Filament
    ) {
        val newFilament = mapOf(
           "id" to  UUID.randomUUID().toString(),
            "type" to filament.type,
            "brand" to filament.brand,
            "weight" to filament.weight,
            "status" to filament.status,
            "color" to String.format("#%08X", filament.color.toArgb()),
            "expirationDate" to filament.expirationDate?.toString(),
            "activeNfc" to filament.activeNfc,
            "note" to filament.note
        )

        db.collection("users").document(userId)
            .update("filaments", FieldValue.arrayUnion(newFilament))

        Notification.scheduleNotification(
            getApplication<Application>().applicationContext,
            filament.id,
            filament.expirationDate
        )
    }

    /**
     * Uloží existujúci filament do Firestore, aktualizuje jeho údaje a prepíše existujúce hodnoty.
     * @param filament Objekt Filament, ktorý sa má uložiť.
     */
    fun saveExistfilament(
        filament: Filament
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userFilaments = document.get("filaments") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val updatedFilaments = userFilaments.map { existingFilament ->
                    if (existingFilament["id"] == filament.id) {
                        mapOf(
                            "id" to filament.id,
                            "type" to filament.type,
                            "brand" to filament.brand,
                            "weight" to filament.weight,
                            "status" to filament.status,
                            "color" to "#${filament.color.value.toULong().toString(16).substring(0, 8)}",
                            "expirationDate" to filament.expirationDate.toString(),
                            "activeNfc" to !filament.activeNfc,
                            "note" to filament.note
                        )
                    } else {
                        existingFilament
                    }
                }

                db.collection("users").document(userId)
                    .update("filaments", updatedFilaments)
            }
    }

    /**
     * Načíta filament podľa jeho ID a aktualizuje currentFilament.
     * @param filamentId ID filamentu, ktorý sa má načítať.
     * @param callback Funkcia, ktorá sa zavolá s výsledkom načítania.
     */
    fun loadFilamentById(
        filamentId: String,
        callback: (Boolean) -> Unit
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userFilaments = document.get("filaments") as? List<Map<String, Any>>
                    val matchingFilament = userFilaments?.find { it["id"] == filamentId }
                    if (matchingFilament != null) {
                        currentFilament.value = Filament(
                            id = matchingFilament["id"] as? String ?: "",
                            type = matchingFilament["type"] as? String ?: "",
                            brand = matchingFilament["brand"] as? String ?: "",
                            weight = (matchingFilament["weight"] as? Number)?.toInt() ?: 0,
                            status = matchingFilament["status"] as? String ?: "",
                            color = Color(android.graphics.Color.parseColor(matchingFilament["color"] as? String ?: "#000000")),
                            expirationDate = (matchingFilament["expirationDate"] as? String).let { LocalDate.parse(it) },
                            activeNfc = (matchingFilament["activeNfc"] as? Boolean) == false,
                            note = matchingFilament["note"] as? String ?: ""
                        )
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    /**
     * Aktualizuje stav NFC pre daný filament podľa jeho ID.
     * @param filamentId ID filamentu, ktorého NFC stav sa má aktualizovať.
     * @param status Nový stav NFC.
     */
    fun updateFilamentNfcStatus(
        filamentId: String,
        status: Boolean
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userFilaments = document.get("filaments") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val updatedFilaments = userFilaments.map { existingFilament ->
                    if (existingFilament["id"] == filamentId) {
                        existingFilament.toMutableMap().apply {
                            this["activeNfc"] = status
                        }
                    } else {
                        existingFilament
                    }
                }

                db.collection("users").document(userId)
                    .update("filaments", updatedFilaments)
            }
    }

    /**
     * Aktualizuje hmotnosť filamentu podľa jeho ID.
     * @param filamentId ID filamentu, ktorého hmotnosť sa má aktualizovať.
     * @param newWeight Nová hmotnosť filamentu.
     */
    fun updateFilamentWeight(
        filamentId: String,
        newWeight: Int
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userFilaments = document.get("filaments") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val updatedFilaments = userFilaments.map { existingFilament ->
                    if (existingFilament["id"] == filamentId) {
                        existingFilament.toMutableMap().apply {
                            this["weight"] = newWeight
                        }
                    } else {
                        existingFilament
                    }
                }

                db.collection("users").document(userId)
                    .update("filaments", updatedFilaments)
            }
    }

    /**
     * Odstráni filament podľa jeho ID z databázy.
     * @param filamentId ID filamentu, ktorý sa má odstrániť.
     */
    fun deleteFilament(
        filamentId: String
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userFilaments = document.get("filaments") as? List<Map<String, Any>> ?: emptyList()
                val filamentToDelete = userFilaments.find { it["id"] == filamentId }

                db.collection("users").document(userId)
                    .update("filaments", FieldValue.arrayRemove(filamentToDelete))
                    .addOnSuccessListener {
                        loadFilaments()
                    }
            }
    }
}