package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.apply
import kotlin.collections.remove

/**
 * ViewModel pre autentifikáciu používateľa.
 * Zodpovedá za prihlasovanie a registráciu používateľov pomocou Firebase Auth a Firestore.
 *
 * @param application Kontext aplikácie potrebný pre AndroidViewModel.
 */
class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {
    internal val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Getter na email aktuálneho používateľa
     */
    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""

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
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        val sharedPref = getApplication<Application>().getSharedPreferences("user_prefs", 0)
                        sharedPref.edit().putString("user_uid", uid).apply()
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

    /**
     * Získanie aktuálne prihláseného používateľa.
     *
     * @param onComplete Funkcia, ktorá sa zavolá s UID používateľa alebo null, ak nie je prihlásený.
     */
    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        val sharedPref = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        sharedPref.edit().remove("user_uid").apply()
        onComplete()
    }

    /**
     * Zmazanie účtu používateľa.
     * Odstráni používateľský účet a vymaže všetky súvisiace dáta z Firestore.
     *
     * @param onComplete Funkcia, ktorá sa zavolá po úspešnom zmazaní účtu.
     */
    fun deleteAccount(onComplete: () -> Unit) {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            val sharedPref = getApplication<Application>().getSharedPreferences("user_prefs", 0)
            sharedPref.edit().remove("user_uid").apply()
            onComplete()
        }
    }

    /**
     * Zmena mailu používateľa.
     * Vyžaduje aktuálne heslo na overenie a potom aktualizuje mail na nový.
     *
     * @param currentPassword Aktuálne heslo používateľa.
     * @param newEmail Nový mail, ktorý sa má nastaviť.
     * @param callback Funkcia, ktorá sa zavolá s výsledkom operácie.
     */
    fun changeEmail(currentPassword: String, newEmail: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            callback(false, "User not logged in")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, updateTask.exception?.localizedMessage ?: "Failed to update email")
                    }
                }
            } else {
                callback(false, authTask.exception?.localizedMessage ?: "Re-authentication failed")
            }
        }
    }

    /**
     * Zmena hesla používateľa.
     * Vyžaduje aktuálne heslo na overenie a potom aktualizuje heslo na nové.
     *
     * @param currentPassword Aktuálne heslo používateľa.
     * @param newPassword Nové heslo, ktoré sa má nastaviť.
     * @param callback Funkcia, ktorá sa zavolá s výsledkom operácie.
     */
    fun changePassword(currentPassword: String, newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            callback(false, "User not logged in")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, updateTask.exception?.localizedMessage ?: "Failed to update password")
                    }
                }
            } else {
                callback(false, authTask.exception?.localizedMessage ?: "Re-authentication failed")
            }
        }
    }

    /**
     * Prihlásenie cez Google
     * @param account Google Sign-In účet
     * @param callback Výsledok prihlásenia (success, error)
     */
    fun signInWithGoogle(account: GoogleSignInAccount, callback: (Boolean, String?) -> Unit) {
        Log.d("GoogleSignIn", "1. Starting Google Sign-In with account: ${account.email}")
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("GoogleSignIn", "2. Got Google credential")

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    Log.d("GoogleSignIn", "3. signInWithCredential completed")
                    if (task.isSuccessful) {
                        Log.d("GoogleSignIn", "4. Firebase Auth successful")
                        val user = auth.currentUser
                        Log.d("GoogleSignIn", "5. Current user: ${user?.uid}")
                        if (user != null) {
                            Log.d("GoogleSignIn", "6. User not null, saving profile...")
                            // Počkaj na uloženie profilu pred callbackom
                            saveOrUpdateUserProfileWithCallback(
                                uid = user.uid,
                                email = user.email ?: "",
                                displayName = user.displayName ?: "",
                                photoUrl = user.photoUrl?.toString() ?: ""
                            ) {
                                Log.d("GoogleSignIn", "7. Profile saved, calling callback(true)")
                                saveUserToLocalStorage(user.uid)
                                callback(true, null)
                            }
                        } else {
                            Log.e("GoogleSignIn", "ERROR: User is null after auth success")
                            callback(false, "User is null")
                        }
                    } else {
                        Log.e("GoogleSignIn", "ERROR: Firebase auth failed - ${task.exception?.message}")
                        callback(false, task.exception?.message)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("GoogleSignIn", "ERROR: signInWithCredential failed - ${exception.message}")
                    callback(false, exception.message)
                }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "ERROR: Exception in signInWithGoogle - ${e.message}", e)
            callback(false, e.message)
        }
    }

    /**
     * Uloženie alebo aktualizácia profilu v Firestore s callbackom
     */
    private fun saveOrUpdateUserProfileWithCallback(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String,
        onComplete: () -> Unit
    ) {
        Log.d("GoogleSignIn", "A. Starting saveOrUpdateUserProfileWithCallback for uid: $uid")
        val userDoc = db.collection("users").document(uid)

        userDoc.get().addOnCompleteListener { task ->
            Log.d("GoogleSignIn", "B. Firestore get() completed")
            if (task.isSuccessful) {
                Log.d("GoogleSignIn", "C. Firestore get() successful")
                if (!task.result.exists()) {
                    Log.d("GoogleSignIn", "D. User doc doesn't exist, creating new...")
                    val userData = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "displayName" to displayName,
                        "photoUrl" to photoUrl,
                        "createdAt" to System.currentTimeMillis(),
                        "lastLoginAt" to System.currentTimeMillis(),
                        "authProvider" to "google",
                        "sessions" to arrayListOf<String>(),
                        "filaments" to arrayListOf<String>()
                    )
                    userDoc.set(userData).addOnCompleteListener { setTask ->
                        Log.d("GoogleSignIn", "E. Firestore set() completed")
                        if (setTask.isSuccessful) {
                            Log.d("GoogleSignIn", "F. Firestore set() successful, calling onComplete()")
                            onComplete()
                        } else {
                            Log.e("GoogleSignIn", "ERROR: Firestore set() failed - ${setTask.exception?.message}")
                            onComplete()
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("GoogleSignIn", "ERROR: Firestore set() failure - ${exception.message}")
                        onComplete()
                    }
                } else {
                    Log.d("GoogleSignIn", "G. User doc exists, updating...")
                    userDoc.update(
                        mapOf(
                            "lastLoginAt" to System.currentTimeMillis(),
                            "displayName" to displayName,
                            "photoUrl" to photoUrl
                        )
                    ).addOnCompleteListener { updateTask ->
                        Log.d("GoogleSignIn", "H. Firestore update() completed")
                        if (updateTask.isSuccessful) {
                            Log.d("GoogleSignIn", "I. Firestore update() successful, calling onComplete()")
                            onComplete()
                        } else {
                            Log.e("GoogleSignIn", "ERROR: Firestore update() failed - ${updateTask.exception?.message}")
                            onComplete()
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("GoogleSignIn", "ERROR: Firestore update() failure - ${exception.message}")
                        onComplete()
                    }
                }
            } else {
                Log.e("GoogleSignIn", "ERROR: Firestore get() failed - ${task.exception?.message}")
                onComplete()
            }
        }.addOnFailureListener { exception ->
            Log.e("GoogleSignIn", "ERROR: Firestore get() failure - ${exception.message}")
            onComplete()
        }
    }

    /**
     * Uloženie UID do lokálneho úložiska
     */
    private fun saveUserToLocalStorage(uid: String) {
        val sharedPref = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        sharedPref.edit().apply {
            putString("user_uid", uid)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Kontrola či je user prihlásený
     */
    fun checkAuthStatus(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Odoslanie verifikačného emailu aktuálnemu používateľovi.
     * Musia byť splnené dve podmienky: používateľ musí byť prihlásený a email nemusí byť overený.
     *
     * @param callback Funkcia, ktorá sa zavolá s výsledkom (success, error).
     */
    fun sendVerificationEmail(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "Používateľ nie je prihlásený")
            return
        }

        if (user.isEmailVerified) {
            callback(true, null)
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Verifikačný email odoslaný: ${user.email}")
                    callback(true, null)
                } else {
                    Log.e("EmailVerification", "Nepodarilo sa odoslať verifikačný email: ${task.exception?.message}")
                    callback(false, task.exception?.message ?: "Odoslanie emailu zlyhalo")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EmailVerification", "Chyba pri odosilaní verifikácie: ${exception.message}")
                callback(false, exception.message ?: "Chyba pri odosilaní emailu")
            }
    }

    /**
     * Preloadovanie aktuálneho používateľa z Firebase Auth.
     * Toto obnovuje údaje o používateľovi vrátane statusu emailovej verifikácie.
     *
     * @param callback Funkcia, ktorá sa zavolá s výsledkom (success, error).
     */
    fun reloadUser(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "Používateľ nie je prihlásený")
            return
        }

        user.reload()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Používateľ obnovený. Email overený: ${user.isEmailVerified}")
                    callback(true, null)
                } else {
                    Log.e("EmailVerification", "Nepodarilo sa obnoviť používateľa: ${task.exception?.message}")
                    callback(false, task.exception?.message ?: "Obnovenie zlyhalo")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EmailVerification", "Chyba pri obnovení používateľa: ${exception.message}")
                callback(false, exception.message ?: "Chyba pri obnovení")
            }
    }

    /**
     * Opätovné odoslanie verifikačného emailu.
     * Môže byť volané viacerými krát, ale je odporúčané implementovať throttling na klientskej strane.
     *
     * @param callback Funkcia, ktorá sa zavolá s výsledkom (success, error).
     */
    fun resendVerificationEmail(callback: (Boolean, String?) -> Unit) {
        sendVerificationEmail(callback)
    }

    /**
     * Getter na zistenie či je email aktuálneho používateľa overený.
     *
     * @return True, ak je email overený, False inak (aj keď user neexistuje).
     */
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }
}