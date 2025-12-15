package com.example.spoolsync.ui.viewModels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spoolsync.data.model.Filament
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FilamentViewModel(
    application: Application
) : AndroidViewModel(application) {
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
                    return@addSnapshotListener
                }

                filaments.clear()
                val userFilaments = snapshot?.get("filaments") as? List<Map<String, Any>>
                userFilaments?.forEach { data ->
                    try {
                        filaments.add(Filament.fromMap(data))
                    } catch (_: Exception) {
                    }
                }
            }
    }

    fun saveNewFilament(filament: Filament) {
        val newFilament = mapOf(
            "id" to java.util.UUID.randomUUID().toString(),
            "type" to filament.type,
            "brand" to filament.brand,
            "weight" to filament.weight,
            "status" to filament.status,
            "color" to String.format("#%08X", filament.color.toArgb()),
            "expirationDate" to filament.expirationDate,
            "activeNfc" to filament.activeNfc,
            "note" to filament.note,
            "ownerId" to userId,
            "originalWeight" to filament.weight
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
                            "color" to String.format("#%08X", filament.color.toArgb()),
                            "expirationDate" to filament.expirationDate,
                            "activeNfc" to filament.activeNfc,
                            "note" to filament.note,
                            "ownerId" to filament.ownerId,
                            "originalWeight" to filament.originalWeight
                        )
                    } else {
                        existingFilament
                    }
                }

                db.collection("users").document(userId)
                    .update("filaments", updatedFilaments)
            }
    }

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
                        currentFilament.value = Filament.fromMap(matchingFilament)
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

    private val _sessions = MutableStateFlow<Map<String, String>>(emptyMap())
    val sessions: StateFlow<Map<String, String>> = _sessions

    data class UiState(val error: String? = null)
    private val _uiState = MutableLiveData(UiState())

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("sessions").get().await()
                val sessionMap = mutableMapOf<String, String>()
                querySnapshot.documents.forEach { doc ->
                    val sessionId = doc.id
                    val sessionName = doc.getString("name") ?: sessionId
                    sessionMap[sessionId] = sessionName
                }
                _sessions.value = sessionMap
            } catch (e: Exception) {
                _sessions.value = emptyMap()
            }
        }
    }

    fun moveFilamentToSession(
        filamentId: String,
        sessionId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val userFilaments =
                    userDoc.get("filaments") as? List<Map<String, Any>> ?: emptyList()
                val filamentToMove = userFilaments.find { it["id"] == filamentId }

                if (filamentToMove != null) {
                    db.collection("users").document(userId)
                        .update("filaments", FieldValue.arrayRemove(filamentToMove))
                        .await()

                    val sessionFilament = mutableMapOf<String, Any>()
                    sessionFilament["id"] = (filamentToMove["id"] as? String) ?: ""
                    sessionFilament["type"] = (filamentToMove["type"] as? String) ?: ""
                    sessionFilament["brand"] = (filamentToMove["brand"] as? String) ?: ""
                    sessionFilament["weight"] =
                        ((filamentToMove["weight"] as? Number)?.toInt()) ?: 0
                    sessionFilament["status"] = (filamentToMove["status"] as? String) ?: ""
                    sessionFilament["color"] = (filamentToMove["color"] as? String) ?: "#FFFFFFFF"
                    sessionFilament["expirationDate"] =
                        (filamentToMove["expirationDate"] as? String) ?: ""
                    sessionFilament["activeNfc"] =
                        (filamentToMove["activeNfc"] as? Boolean) ?: false
                    sessionFilament["note"] = (filamentToMove["note"] as? String) ?: ""
                    sessionFilament["ownerId"] = userId
                    sessionFilament["originalWeight"] =
                        ((filamentToMove["originalWeight"] as? Number)?.toInt())
                            ?: ((filamentToMove["weight"] as? Number)?.toInt()) ?: 0

                    db.collection("sessions").document(sessionId)
                        .update("filaments", FieldValue.arrayUnion(sessionFilament))
                        .await()

                    loadFilaments()
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(error = e.message)
            }
        }
    }
}