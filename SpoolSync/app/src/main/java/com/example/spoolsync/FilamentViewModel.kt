package com.example.spoolsync

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.spoolsync.ui.Filament
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
                            expirationDate = data["expirationDate"] as? String ?: ""
                        )
                    )
                }
            }
    }

    fun addFilament(filament: Filament) {
        val newFilament = hashMapOf(
            "type" to filament.type,
            "brand" to filament.brand,
            "weight" to filament.weight,
            "status" to filament.status
        )

        db.collection("users").document(userId)
            .update("filaments", FieldValue.arrayUnion(newFilament))
            .addOnSuccessListener {
                Log.d("FilamentViewModel", "Filament added")
            }
            .addOnFailureListener { e ->
                Log.e("FilamentViewModel", "Error adding filament", e)
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
                            expirationDate = matchingFilament["expirationDate"] as? String ?: ""
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

    // Pridajte túto funkciu pre prázdny tag
    fun createNewFilament() {
        currentFilament.value = Filament("", "", "", "", "", "", "")
    }
}