package com.example.spoolsync.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Obrazovka pre prihlásenie používateľa.
 * Umožňuje zadať email a heslo, spracováva prihlásenie a naviguje po úspešnom prihlásení.
 *
 * @param navController Navigácia v aplikácii.
 * @param authViewModel ViewModel pre autentifikáciu používateľa.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val error1 = stringResource(R.string.error1)
    val error2 = stringResource(R.string.error2)
    val signInWithGoogleText = stringResource(R.string.sign_in_with_google)
    val context = LocalContext.current

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In Launcher result: resultCode=${result.resultCode}")
        Log.d("LoginScreen", "Result data: ${result.data}")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("LoginScreen", "Result OK, parsing intent...")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("LoginScreen", "Got account: ${account.email}")
                isLoading = true
                errorMessage = null

                Log.d("LoginScreen", "Calling authViewModel.signInWithGoogle...")
                authViewModel.signInWithGoogle(account) { success, error ->
                    Log.d("LoginScreen", "Callback received: success=$success, error=$error")
                    isLoading = false
                    if (success) {
                        Log.d("LoginScreen", "Success! Navigating to filaments...")
                        navController.navigate("filaments") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Log.e("LoginScreen", "Failed: $error")
                        errorMessage = error ?: "Google prihlásenie zlyhalo"
                    }
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "ApiException: ${e.message}", e)
                Log.e("LoginScreen", "ApiException statusCode: ${e.statusCode}")
                isLoading = false
                errorMessage = "Google prihlásenie zlyhalo: ${e.message}"
            }
        } else {
            Log.d("LoginScreen", "Result not OK (probably user cancelled or error)")
            Log.d("LoginScreen", "Checking for exception in intent...")
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                Log.e("LoginScreen", "EXCEPTION FOUND - ApiException statusCode: ${e.statusCode}, message: ${e.message}", e)
                isLoading = false
                errorMessage = when (e.statusCode) {
                    12501 -> "Google Sign-In bol zrušený"
                    12500 -> "Google Play Services nie sú dostupné alebo nie sú aktualizované"
                    else -> "Google Sign-In chyba: ${e.message}"
                }
            } catch (ex: Exception) {
                Log.e("LoginScreen", "Other exception: ${ex.message}", ex)
                isLoading = false
                errorMessage = "Chyba pri Google Sign-In: ${ex.message}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.banner_light),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email), color = colorResource(R.color.dark_gray)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.light_gray),
                unfocusedBorderColor = colorResource(R.color.light_gray),
                cursorColor = colorResource(R.color.light_gray),
                unfocusedLabelColor = colorResource(R.color.light_gray),
                focusedLabelColor = colorResource(R.color.light_gray),
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password), color = colorResource(R.color.dark_gray)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.light_gray),
                unfocusedBorderColor = colorResource(R.color.light_gray),
                cursorColor = colorResource(R.color.light_gray),
                unfocusedLabelColor = colorResource(R.color.light_gray),
                focusedLabelColor = colorResource(R.color.light_gray),
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = error1
                } else {
                    isLoading = true
                    errorMessage = null
                    authViewModel.loginUser(email, password) { success, errorMsg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = errorMsg ?: error2
                        } else {
                            // Skontroluj či je email overený
                            if (authViewModel.isEmailVerified()) {
                                navController.navigate("filaments") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                // Email nie je overený, naviguj na verification screen
                                navController.navigate("verification") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpoolSyncTheme.colors.lightGrayGray
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = colorResource(R.color.white))
            } else {
                Text(stringResource(R.string.login), color = colorResource(R.color.white))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.or),
                modifier = Modifier.padding(horizontal = 8.dp),
                color = colorResource(R.color.gray)
            )
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Button
        Button(
            onClick = {
                Log.d("LoginScreen", "Google Sign-In button clicked")
                isLoading = true
                errorMessage = null
                try {
                    Log.d("LoginScreen", "Creating GoogleSignInOptions...")
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.google_web_client_id))
                        .requestEmail()
                        .build()
                    Log.d("LoginScreen", "Getting Google SignIn client...")
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    Log.d("LoginScreen", "Launching Google Sign-In intent...")
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Exception in onClick: ${e.message}", e)
                    isLoading = false
                    errorMessage = "Chyba pri spustení Google prihlásenia: ${e.message}"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4285F4)
            ),
            enabled = !isLoading
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                signInWithGoogleText,
                color = Color.White
            )
        }

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.to_register),
                color = colorResource(R.color.gray)
            )
        }
    }
}