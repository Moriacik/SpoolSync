package com.example.spoolsync.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel pre autentifikáciu používateľa.
 * Zodpovedá za prihlasovanie a registráciu používateľov pomocou Firebase Auth a Firestore.
 *
 * @param application Kontext aplikácie potrebný pre AndroidViewModel.
 */
class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Kontrola, či je používateľ úspešne prihlásený.
     *
     * @param email Email používateľa.
     * @param password Heslo používateľa.
     * @param callback Funkcia, ktorá sa zavolá s výsledkom prihlásenia.
     * @return True, ak je používateľ prihlásený, inak False.
     */
    fun loginUser(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException -> "Používateľ neexistuje"
                            is FirebaseAuthInvalidCredentialsException -> "Nesprávne heslo"
                            else -> "Prihlásenie zlyhalo: ${exception?.message ?: "Neznáma chyba"}"
                        }
                        callback(false, errorMessage)
                    }
                }
                .addOnFailureListener { e ->
                    callback(false, "Chyba prihlásenia: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Chyba: ${e.message}")
        }
    }

    /**
     * Registrácia nového používateľa.
     * Vytvorí používateľský účet a uloží profil do Firestore.
     *
     * @param email Email používateľa.
     * @param password Heslo používateľa.
     * @param onSuccess Funkcia, ktorá sa zavolá pri úspešnej registrácii.
     * @param onError Funkcia, ktorá sa zavolá pri chybe registrácie s chybovou správou.
     */
    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis(),
                        "filaments" to arrayListOf<HashMap<String, String>>()
                    )

                    db.collection("users").document(auth.currentUser?.uid ?: "")
                        .set(user)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onError(e.localizedMessage ?: "Failed to create user profile")
                        }
                } else {
                    onError(task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }
}