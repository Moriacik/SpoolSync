package com.example.spoolsync.viewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import com.example.spoolsync.notification.Notification
import com.example.spoolsync.screens.Filament
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.UUID

class FilamentViewModel(application: Application) : AndroidViewModel(application) {
    private val db: FirebaseFirestore = Firebase.firestore
    private var userId = Firebase.auth.currentUser?.uid ?: ""
    val currentFilament = mutableStateOf<Filament?>(null)
    val filaments = mutableStateListOf<Filament>()

    fun setUserId(newUserId: String) {
        userId = newUserId
        loadFilaments()
    }


    fun loadFilaments() {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FilamentViewModel", "Error loading filaments", error)
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
                    } catch (e: Exception) {
                        Log.e("FilamentViewModel", "Error parsing filament: ${e.message}")
                    }
                }
            }
    }

    fun saveNewFilament(filament: Filament) {
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
            filament.type,
            filament.expirationDate
        )
    }

    fun saveExistfilament(filament: Filament) {
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
                            "color" to filament.color,
                            "expirationDate" to filament.expirationDate,
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

    fun loadFilamentById(filamentId: String, callback: (Boolean) -> Unit) {
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

    fun updateFilamentNfcStatus(filamentId: String, status: String) {
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

    fun updateFilamentWeight(filamentId: String, newWeight: Int) {
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

    fun deleteFilament(filamentId: String) {
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