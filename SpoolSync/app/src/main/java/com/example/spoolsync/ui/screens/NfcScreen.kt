package com.example.spoolsync.ui.screens

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.spoolsync.ui.viewModels.FilamentViewModel
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spoolsync.R
import com.example.spoolsync.ui.viewModels.NfcViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Režimi obrazovky pre prácu s NFC tagmi filamentu.
 */
enum class FilamentNfcScreenMode {
    READ, UPDATE, OCR
}

/**
 * Obrazovka pre prácu s NFC tagmi filamentu.
 * Umožňuje čítanie, aktualizáciu alebo získanie ID filamentu pomocou NFC podľa zvoleného režimu.
 *
 * @param navController Navigácia v aplikácii.
 * @param filamentViewModel ViewModel pre správu filamentov.
 * @param nfcViewModel ViewModel pre operácie s NFC.
 * @param mode Režim obrazovky.
 * @param filamentId ID filamentu (voliteľné, používa sa pri aktualizácii).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentNfcScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    nfcViewModel: NfcViewModel,
    mode: FilamentNfcScreenMode,
    filamentId: String? = null
) {
    val context = LocalContext.current
    var nfcId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    val coroutineScope = rememberCoroutineScope()

    var error1 = stringResource(R.string.nfc_error1)
    var error2 = stringResource(R.string.filament_not_found)

    var errors = listOf(
        stringResource(R.string.nfc_error2),
        stringResource(R.string.nfc_error3),
        stringResource(R.string.nfc_error4),
        stringResource(R.string.nfc_error5),
        stringResource(R.string.nfc_error6)
    )

    DisposableEffect(Unit) {
        val nfcCallback = object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag?) {
                if (mode == FilamentNfcScreenMode.OCR) {
                    nfcViewModel.readNfcTag(
                        tag,
                        error1,
                        { id ->
                            coroutineScope.launch {
                                withContext(Dispatchers.Main) {
                                    nfcId = id
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("filamentId", id)
                                    navController.popBackStack()
                                }
                            }
                        },
                        { error -> errorMessage = error }
                    )
                } else if (mode == FilamentNfcScreenMode.READ) {
                    nfcViewModel.readNfcTag(
                        tag,
                        error1,
                        { id ->
                            nfcId = id
                            coroutineScope.launch {
                                filamentViewModel.loadFilamentById(nfcId) { success ->
                                    if (success) {
                                        navController.navigate("filamentView/$nfcId")
                                    } else {
                                        errorMessage = error2
                                    }
                                }
                            }
                        },
                        { error -> errorMessage = error }
                    )
                } else if (mode == FilamentNfcScreenMode.UPDATE && filamentId != null) {
                    nfcViewModel.updateNfcTag(
                        filamentId,
                        tag,
                        errors,
                        onSuccess = {
                            nfcId = filamentId
                            coroutineScope.launch {
                                navController.navigate("filamentView/$filamentId")
                                filamentViewModel.updateFilamentNfcStatus(filamentId, true)
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
            context as Activity,
            nfcCallback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            null
        )

        onDispose {
            nfcAdapter?.disableReaderMode(context as Activity)
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
                            contentDescription = stringResource(R.string.info),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.info),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = colorResource(R.color.nav_bar_light)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filaments") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_filament),
                                contentDescription = stringResource(R.string.filaments),
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(48.dp),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {  },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info),
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("ocr") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_printer),
                                contentDescription = stringResource(R.string.print),
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
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
                contentDescription = stringResource(R.string.nfc),
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )

            Text(
                text = stringResource(R.string.place_device_close_to_tag),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.gray)
            )

            if (mode == FilamentNfcScreenMode.READ) {
                Text(
                    text = stringResource(R.string.or),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.gray)
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = nfcId,
                    onValueChange = { nfcId = it },
                    label = { Text(stringResource(R.string.enter_id_manually), color = colorResource(R.color.dark_gray)) },
                    modifier = Modifier
                        .width(280.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.light_gray),
                        unfocusedBorderColor = colorResource(R.color.light_gray),
                        cursorColor = colorResource(R.color.light_gray),
                        unfocusedLabelColor = colorResource(R.color.light_gray),
                        focusedLabelColor = colorResource(R.color.light_gray),
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
                                    errorMessage = error2
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
                        containerColor = colorResource(R.color.light_gray),
                        contentColor = colorResource(R.color.dark_gray)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colorResource(R.color.white),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.submit))
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.red),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}