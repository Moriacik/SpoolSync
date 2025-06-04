package com.example.spoolsync.screens

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.spoolsync.viewModels.FilamentViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import kotlin.collections.get
import kotlin.text.toInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentNfcScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel = viewModel(),
    mode: FilamentNfcScreenMode,
    filamentId: String? = null
) {
    val context = LocalContext.current
    var nfcId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    var lightGrayColor = colorResource(R.color.light_gray)
    var darkGrayColor = colorResource(R.color.dark_gray)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val nfcCallback = object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag?) {
                if (mode == FilamentNfcScreenMode.READ) {
                    readNfcTag(tag, { id ->
                        nfcId = id
                    }, { error ->
                        errorMessage = error
                    })
                } else if (mode == FilamentNfcScreenMode.UPDATE && filamentId != null) {
                    updateNfcTag(
                        filamentId,
                        tag,
                        filamentViewModel,
                        onSuccess = {
                            nfcId = filamentId
                            coroutineScope.launch {
                                navController.navigate("filamentView/$filamentId")
                            }
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
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
                            painter = painterResource(R.drawable.ic_info),
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
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.nfc),
                contentDescription = "NFC Icon",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )

            Text(
                text = "Place device close to tag",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (mode == FilamentNfcScreenMode.READ) {
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = nfcId,
                    onValueChange = { nfcId = it },
                    label = { Text("Enter ID manually", color = darkGrayColor) },
                    modifier = Modifier
                        .width(280.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = lightGrayColor,
                        unfocusedBorderColor = lightGrayColor,
                        cursorColor = lightGrayColor,
                        unfocusedLabelColor = lightGrayColor,
                        focusedLabelColor = lightGrayColor,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier
                        .height(50.dp)
                        .width(280.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightGrayColor,
                        contentColor = darkGrayColor
                    )
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

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

fun readNfcTag(
    tag: Tag?,
    onNfcRead: (String) -> Unit,
    onError: (String) -> Unit
) {
    tag?.let {
        val ndef = Ndef.get(tag)
        try {
            ndef?.connect()
            val message = ndef?.ndefMessage
            val payload = message?.records?.get(0)?.payload
            if (payload != null) {
                val languageCodeLength = payload[0].toInt() and 0x3F
                val nfcId = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charsets.UTF_8)
                onNfcRead(nfcId)
            }
        } catch (e: Exception) {
            onError("Error reading NFC tag: ${e.message}")
        } finally {
            ndef?.close()
        }
    }
}

fun updateNfcTag(
    filamentId: String,
    tag: Tag?,
    filamentViewModel: FilamentViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    tag?.let {
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()

            if (ndef != null) {
                // Make sure the tag is writable
                if (!ndef.isWritable) {
                    onError("NFC tag is not writable")
                    return
                }

                // Get max size
                val maxSize = ndef.maxSize
                if (filamentId.length > maxSize) {
                    onError("Data too large for NFC tag")
                    return
                }

                // Create and write the message
                val record = NdefRecord.createTextRecord("en", filamentId)
                val message = NdefMessage(arrayOf(record))
                ndef.writeNdefMessage(message)

                filamentViewModel.updateFilamentNfcStatus(filamentId, "true")

                onSuccess()
            } else {
                onError("Tag doesn't support NDEF")
            }
        } catch (e: Exception) {
            onError("Error updating NFC tag: ${e.message}")
        } finally {
            try {
                Ndef.get(tag)?.close()
            } catch (e: Exception) { }
        }
    } ?: onError("Tag is null")
}

enum class FilamentNfcScreenMode {
    READ, UPDATE
}