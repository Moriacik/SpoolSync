package com.example.spoolsync.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.AccountActionButton
import com.example.spoolsync.ui.viewModels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Obrazovka účtu používateľa.
 * Zobrazuje informácie o používateľovi a umožňuje vykonávanie určitých akcií.
 *
 * @param navController Navigácia v aplikácii.
 * @param authViewModel ViewModel pre autentifikáciu používateľa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var newEmailInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.account), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    )
                    {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = colorResource(R.color.dark_gray),
                    modifier = Modifier
                        .size(100.dp)
                        .background(colorResource(R.color.light_gray), CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user?.email ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UID: ${user?.uid ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.gray)
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                AccountActionButton(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.change_email),
                    onClick = { showChangeEmailDialog = true }
                )

                AccountActionButton(
                    icon = Icons.Default.Lock,
                    label = stringResource(R.string.change_password),
                    onClick = { showChangePasswordDialog = true }
                )

                AccountActionButton(
                    icon = Icons.Default.ExitToApp,
                    label = stringResource(R.string.sign_out),
                    onClick = { showSignOutDialog = true }
                )

                AccountActionButton(
                    icon = Icons.Default.Delete,
                    label = stringResource(R.string.delete_account),
                    onClick = { showDeleteDialog = true },
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.application),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                AccountActionButton(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.contact_support),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@example.com")
                            putExtra(Intent.EXTRA_SUBJECT, "SpoolSync Support")
                        }
                        context.startActivity(intent)
                    }
                )

                Text(
                    text = "SpoolSync ${stringResource(R.string.version)} : 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.gray)
                )
            }
        }

        /**
         * Dialóg: Odhlásenie.
         */
        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text(stringResource(R.string.sign_out)) },
                text = { Text(stringResource(R.string.sign_out_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        showSignOutDialog = false
                        authViewModel.signOut {
                            navController.navigate("login") {
                                popUpTo("account") { inclusive = true }
                            }
                        }
                    }) { Text(stringResource(R.string.submit)) }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        /**
         * Dialóg: Zmazanie účtu.
         */
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_account)) },
                text = { Text(stringResource(R.string.delete_account_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount {
                            navController.navigate("login") {
                                popUpTo("account") { inclusive = true }
                            }
                        }
                    }) { Text(stringResource(R.string.submit)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        /**
         * Dialóg: Zmena emailu.
         */
        if (showChangeEmailDialog) {
            AlertDialog(
                onDismissRequest = { showChangeEmailDialog = false },
                title = { Text(stringResource(R.string.change_email)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newEmailInput,
                            onValueChange = { newEmailInput = it },
                            label = { Text(stringResource(R.string.new_email)) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(stringResource(R.string.current_password)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        dialogMessage?.let { Text(it, color = Color.Red) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        authViewModel.changeEmail(passwordInput, newEmailInput) { success, error ->
                            if (success) {
                                dialogMessage = context.getString(R.string.email_changed)
                                showChangeEmailDialog = false
                                passwordInput = ""
                                newEmailInput = ""
                            } else {
                                dialogMessage = error
                            }
                        }
                    }) { Text(stringResource(R.string.submit)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showChangeEmailDialog = false
                        dialogMessage = null
                        passwordInput = ""
                        newEmailInput = ""
                    }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        /**
         * Dialóg: Zmena hesla.
         */
        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                title = { Text(stringResource(R.string.change_password)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text(stringResource(R.string.new_password)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(stringResource(R.string.current_password)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        dialogMessage?.let { Text(it, color = Color.Red) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        authViewModel.changePassword(passwordInput, newPasswordInput) { success, error ->
                            if (success) {
                                dialogMessage = context.getString(R.string.password_changed)
                                showChangePasswordDialog = false
                                passwordInput = ""
                                newPasswordInput = ""
                            } else {
                                dialogMessage = error
                            }
                        }
                    }) { Text(stringResource(R.string.submit)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showChangePasswordDialog = false
                        dialogMessage = null
                        passwordInput = ""
                        newPasswordInput = ""
                    }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}