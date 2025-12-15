package com.example.spoolsync.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

data class QRData(
    val sessionId: String,
    val requestToken: String,
    val expiresAt: Long
)

class StatisticsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun confirmQRCode(qrData: QRData) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Overenie, že token nie je expirovaný
                if (System.currentTimeMillis() > qrData.expiresAt) {
                    _errorMessage.value = "QR kód už vypršal"
                    _isLoading.value = false
                    return@launch
                }

                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _errorMessage.value = "Používateľ nie je prihlásený"
                    _isLoading.value = false
                    return@launch
                }

                // Potvrdenie v Firestore
                firestore.collection("qrSessions")
                    .document(qrData.sessionId)
                    .update(
                        mapOf(
                            "confirmedBy" to userId,
                            "confirmed" to true,
                            "confirmedAt" to System.currentTimeMillis()
                        )
                    )
                    .addOnSuccessListener {
                        _successMessage.value = "Štatistiky boli exportované"
                        _isLoading.value = false
                    }
                    .addOnFailureListener { exception ->
                        _errorMessage.value = exception.message ?: "Chyba pri potvrdení"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Neznáma chyba"
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}