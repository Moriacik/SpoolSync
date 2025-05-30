package com.example.spoolsync.ui

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.spoolsync.FilamentViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spoolsync.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentNfcScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel = viewModel()
) {
    val context = LocalContext.current
    var nfcId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    DisposableEffect(Unit) {
        val nfcCallback = object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag?) {
                tag?.let {
                    val ndef = Ndef.get(tag)
                    try {
                        ndef?.connect()
                        val message = ndef?.ndefMessage
                        val payload = message?.records?.get(0)?.payload
                        if (payload != null) {
                            val languageCodeLength = payload[0].toInt() and 0x3F
                            nfcId = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charsets.UTF_8)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error reading NFC tag: ${e.message}"
                    } finally {
                        ndef?.close()
                    }
                }
            }
        }

        nfcAdapter?.enableReaderMode(
            context as android.app.Activity,
            nfcCallback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            null
        )

        onDispose {
            nfcAdapter?.disableReaderMode(context as android.app.Activity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.info),
                            contentDescription = "Info",
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Filament Info",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = colorResource(R.color.nav_bar)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filaments") },
                        icon = { Text("Filamenty") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already on info screen */ },
                        icon = { Text("Info") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("print") },
                        icon = { Text("Tlačiť") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NFC Icon
            Image(
                painter = painterResource(R.drawable.status),
                contentDescription = "NFC Icon",
                modifier = Modifier
                    .size(120.dp)
                    .padding(32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NFC",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Place device close to tag",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "or",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Manual ID Input
            OutlinedTextField(
                value = nfcId,
                onValueChange = { nfcId = it },
                label = { Text("Enter ID manually") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (nfcId.isNotEmpty()) {
                        isLoading = true
                        filamentViewModel.loadFilamentById(nfcId) { success ->
                            isLoading = false
                            if (success) {
                                navController.navigate("filamentView/$nfcId")
                            } else {
                                errorMessage = "Filament not found"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue")
                }
            }
        }
    }
}