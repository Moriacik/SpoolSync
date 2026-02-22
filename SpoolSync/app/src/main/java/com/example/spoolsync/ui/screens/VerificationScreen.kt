package com.example.spoolsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.viewModels.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Obrazovka na verifikáciu emailu po registrácii.
 * Zobrazuje pokyny na overenie emailu, tlačidlo na opätovné poslanie emailu a tlačidlo na skontrolovanie statusu.
 *
 * @param navController Navigácia v aplikácii.
 * @param authViewModel ViewModel pre autentifikáciu používateľa.
 */
@Composable
fun VerificationScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    val userEmail = authViewModel.getCurrentUserEmail()

    // Automaticky odošli verifikačný email pri načítaní
    LaunchedEffect(Unit) {
        authViewModel.sendVerificationEmail { success, error ->
            if (!success) {
                val isBlockedError = error?.contains("blocked", ignoreCase = true) == true
                errorMessage = if (isBlockedError) {
                    "Firebase pozastavil požiadavky z tohto zariadenia. Prosím, čakajte pár minút pred opätovným pokusom alebo sa pokúste z iného WiFi/mobilnej siete."
                } else {
                    "Chyba pri odosilaní verifikačného emailu: $error"
                }
            }
        }
    }

    // Cooldown počítadlo pre opätovné poslanie
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
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
        Text(
            text = stringResource(R.string.verify_email_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.verify_email_description, userEmail),
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.dark_gray),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Chybová správa
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = colorResource(R.color.red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Úspešná správa
        if (successMessage != null) {
            Text(
                text = successMessage!!,
                color = colorResource(R.color.green),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tlačidlo na kontrolu verifikácie
        Button(
            onClick = {
                isChecking = true
                errorMessage = null
                successMessage = null

                authViewModel.reloadUser { success, error ->
                    if (success) {
                        if (authViewModel.isEmailVerified()) {
                            successMessage = "Email overený! Navigovanie..."
                            // Čakaj pred navigáciou
                            navController.navigate("filaments") {
                                popUpTo("verification") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Email ešte nie je overený. Skontroluj svoju doručenú poštu."
                        }
                    } else {
                        errorMessage = "Chyba pri overovaní: $error"
                    }
                    isChecking = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isChecking && resendCooldown == 0
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.verify_email_check))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tlačidlo na opätovné poslanie emailu
        TextButton(
            onClick = {
                if (resendCooldown == 0) {
                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    authViewModel.resendVerificationEmail { success, error ->
                        if (success) {
                            successMessage = "Verifikačný email bol opätovne odoslaný."
                            resendCooldown = 60 // 60 sekúnd cooldown
                        } else {
                            // Skontroluj či je to chyba "blocked due to unusual activity"
                            val isBlockedError = error?.contains("blocked", ignoreCase = true) == true
                            errorMessage = if (isBlockedError) {
                                "Firebase pozastavil požiadavky z tohto zariadenia kvôli nezvyčajnej aktivite. Skúste znova za pár minút alebo sa pokúste z iného WiFi/mobilnej siete."
                            } else {
                                "Nepodarilo sa odoslať email: $error"
                            }
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && resendCooldown == 0
        ) {
            if (resendCooldown > 0) {
                Text("Znovu poslať email (${resendCooldown}s)", color = colorResource(R.color.light_gray))
            } else {
                Text(stringResource(R.string.verify_email_resend))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tlačidlo na odhlásenie
        TextButton(
            onClick = {
                authViewModel.signOut {
                    navController.navigate("login") {
                        popUpTo("verification") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.logout), color = colorResource(R.color.light_gray))
        }
    }
}

