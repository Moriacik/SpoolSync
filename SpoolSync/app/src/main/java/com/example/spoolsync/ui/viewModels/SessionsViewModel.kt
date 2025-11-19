package com.example.spoolsync.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spoolsync.data.model.Session
import com.example.spoolsync.data.repository.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionsViewModel : ViewModel() {

    private val sessionRepository = SessionRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance()
    )

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadUserSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.getUserSessions()
            result.onSuccess { sessions ->
                _sessions.value = sessions
                _errorMessage.value = null
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun createSession(sessionName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.createSession(sessionName)
            result.onSuccess { session ->
                _currentSession.value = session
                loadUserSessions()
                _errorMessage.value = null
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun joinSession(accessCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.joinSession(accessCode)
            result.onSuccess { session ->
                _currentSession.value = session
                loadUserSessions()
                _errorMessage.value = null
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.getSession(sessionId)
            result.onSuccess { session ->
                _currentSession.value = session
                _errorMessage.value = null
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun leaveSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.leaveSession(sessionId)
            result.onSuccess {
                _currentSession.value = null
                loadUserSessions()
                _errorMessage.value = null
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun addFilamentToSession(sessionId: String, filamentId: String, ownerId: String, originalWeight: Int) {
        viewModelScope.launch {
            val result = sessionRepository.addFilamentToSession(sessionId, filamentId, ownerId, originalWeight)
            result.onSuccess {
                loadSession(sessionId)
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }

    fun removeFilamentFromSession(sessionId: String, filamentId: String) {
        viewModelScope.launch {
            val result = sessionRepository.removeFilamentFromSession(sessionId, filamentId)
            result.onSuccess {
                loadSession(sessionId)
            }
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }

    fun updateFilamentWeightInSession(sessionId: String, filamentId: String, newWeight: Int) {
        viewModelScope.launch {
            val result = sessionRepository.updateFilamentWeightInSession(sessionId, filamentId, newWeight)
            result.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
}