package com.example.spoolsync.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.AccountActionButton
import com.example.spoolsync.ui.viewModels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.account), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },)
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile picture placeholder
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .background(colorResource(R.color.light_gray), CircleShape),
                tint = colorResource(R.color.dark_gray)
            )

            // User info
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

            // Change email
            AccountActionButton(
                icon = Icons.Default.Email,
                label = stringResource(R.string.change_email),
                onClick = { /* TODO: Implement change email dialog */ }
            )

            // Change password
            AccountActionButton(
                icon = Icons.Default.Lock,
                label = stringResource(R.string.change_password),
                onClick = { /* TODO: Implement change password dialog */ }
            )

            // Sign out
            AccountActionButton(
                icon = Icons.Default.ExitToApp,
                label = stringResource(R.string.sign_out),
                onClick = { showSignOutDialog = true }
            )

            // Delete account
            AccountActionButton(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.delete_account),
                onClick = { showDeleteDialog = true },
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App version/about
            Text(
                text = "1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.gray)
            )

            // Support/contact
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
        }

        // Sign out dialog
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

        // Delete account dialog
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
    }
}