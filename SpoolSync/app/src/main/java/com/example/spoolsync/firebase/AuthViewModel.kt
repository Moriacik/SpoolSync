package com.example.spoolsync.firebase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val TAG = "AuthViewModel"

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Login successful")
                        callback(true, null)
                    } else {
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException -> "Používateľ neexistuje"
                            is FirebaseAuthInvalidCredentialsException -> "Nesprávne heslo"
                            else -> "Prihlásenie zlyhalo: ${exception?.message ?: "Neznáma chyba"}"
                        }
                        Log.e(TAG, "Login failed: $errorMessage", exception)
                        callback(false, errorMessage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Login error: ${e.message}", e)
                    callback(false, "Chyba prihlásenia: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login", e)
            callback(false, "Chyba: ${e.message}")
        }
    }

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mapOf(
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(auth.currentUser?.uid ?: "")
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "User registered successfully")
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Firestore error: ${e.message}", e)
                                callback(false, "Chyba pri vytváraní profilu: ${e.message}")
                            }
                    } else {
                        val errorMessage = "Registrácia zlyhala: ${task.exception?.message ?: "Neznáma chyba"}"
                        Log.e(TAG, errorMessage, task.exception)
                        callback(false, errorMessage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Registration error: ${e.message}", e)
                    callback(false, "Chyba registrácie: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during registration", e)
            callback(false, "Chyba: ${e.message}")
        }
    }
}