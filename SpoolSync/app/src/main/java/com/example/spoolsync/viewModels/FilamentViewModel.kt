package com.example.spoolsync.viewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.spoolsync.screens.Filament
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID
import kotlin.text.get

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
                    filaments.add(
                        Filament(
                            id = data["id"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            brand = data["brand"] as? String ?: "",
                            weight = data["weight"] as? String ?: "",
                            status = data["status"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            expirationDate = data["expirationDate"] as? String ?: "",
                            activeNfc = data["activeNfc"] as? String ?: "",
                            note = data["note"] as? String ?: ""
                        )
                    )
                }
            }
    }

    fun saveNewFilament(filament: Filament) {
        val newFilament = Filament(
            id = UUID.randomUUID().toString(),
            type = filament.type,
            brand = filament.brand,
            weight = filament.weight,
            status = filament.status,
            color = filament.color,
            expirationDate = filament.expirationDate,
            activeNfc = filament.activeNfc,
            note = filament.note
        )

        db.collection("users").document(userId)
            .update("filaments", FieldValue.arrayUnion(newFilament))
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
                            weight = matchingFilament["weight"] as? String ?: "",
                            status = matchingFilament["status"] as? String ?: "",
                            color = matchingFilament["color"] as? String ?: "",
                            expirationDate = matchingFilament["expirationDate"] as? String ?: "",
                            activeNfc = matchingFilament["activeNfc"] as? String ?: "",
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
}