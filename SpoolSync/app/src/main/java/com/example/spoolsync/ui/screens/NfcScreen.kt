package com.example.spoolsync.ui.screens

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.spoolsync.ui.viewModels.FilamentViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.spoolsync.ui.components.NavigationItem
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.BottomNavigationBar
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.NfcViewModel
import com.example.spoolsync.ui.viewModels.SessionsViewModel
import com.example.spoolsync.nfc.NfcTagManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Režimy obrazovky pre prácu s NFC tagmi filamentu.
 */
enum class FilamentNfcScreenMode {
    READ, UPDATE, OCR
}

/**
 * Obrazovka pre prácu s NFC tagmi filamentu.
 * Umožňuje čítanie, aktualizáciu alebo získanie ID filamentu pomocou NFC podľa zvoleného režimu.
 *
 * Teraz používa Intent dispatch prístup (cez NfcTagManager) namiesto reader mode.
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
    filamentId: String? = null,
    sessionId: String? = null,
    sessionsViewModel: SessionsViewModel? = null
) {
    val context = LocalContext.current
    var nfcId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var displayedTagData by remember { mutableStateOf("") }
    var operationComplete by remember { mutableStateOf(false) }
    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val error1 = stringResource(R.string.nfc_error1)
    val error2 = stringResource(R.string.filament_not_found)

    val errors = listOf(
        stringResource(R.string.nfc_error2),
        stringResource(R.string.nfc_error3),
        stringResource(R.string.nfc_error4),
        stringResource(R.string.nfc_error5),
        stringResource(R.string.nfc_error6)
    )

    // Helper function to safely disable reader mode
    val disableReaderMode = {
        try {
            nfcAdapter?.disableReaderMode(context as Activity)
            Log.d("NfcScreen", "Reader mode disabled")
        } catch (e: Exception) {
            Log.w("NfcScreen", "Failed to disable reader mode: ${e.message}")
        }
    }

    // NFC callback for reader mode - must be before DisposableEffect
    val nfcCallback = remember {
        object : NfcAdapter.ReaderCallback {
            override fun onTagDiscovered(tag: Tag?) {
                // Only guard against truly repeated callbacks after we finish
                if (operationComplete) {
                    Log.d("NfcScreen", "Operation already complete, ignoring tag")
                    return
                }
                Log.d("NfcScreen", "onTagDiscovered: tag=$tag, mode=$mode")
                // Forward tag to state for processing in LaunchedEffect
                NfcTagManager.setTag(tag, navigateToNfc = false)
            }
        }
    }

    // Listen for NFC tags from reader mode
    val nfcTag by NfcTagManager.nfcTag.collectAsState()

    // Enable/disable reader mode on lifecycle events
    DisposableEffect(lifecycleOwner, nfcAdapter, nfcCallback) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("NfcScreen", "ON_RESUME - enabling reader mode")
                    val activity = context as Activity
                    if (nfcAdapter == null || !nfcAdapter.isEnabled) {
                        errorMessage = error1
                    } else {
                        errorMessage = ""
                        try {
                            nfcAdapter.enableReaderMode(
                                activity,
                                nfcCallback,
                                // Use only necessary tech flags and silence platform sounds
                                NfcAdapter.FLAG_READER_NFC_A or
                                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                                null
                            )
                            Log.d("NfcScreen", "Reader mode enabled")
                        } catch (e: Exception) {
                            Log.e("NfcScreen", "Failed to enable NFC listening", e)
                            errorMessage = e.message ?: error2
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("NfcScreen", "ON_PAUSE - disabling NFC listening")
                    disableReaderMode()
                }
                else -> {}
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            disableReaderMode()
        }
    }

    // Process tag when it's received
    LaunchedEffect(nfcTag) {
        if (nfcTag != null && !operationComplete) {
            Log.d("NfcScreen", "NFC tag received from NfcTagManager, mode=$mode")
            displayedTagData = "Tag ID: ${nfcTag!!.id.joinToString("") { "%02X".format(it) }}"
            val tag = nfcTag

            if (mode == FilamentNfcScreenMode.OCR) {
                nfcViewModel.readNfcTag(
                    tag,
                    error1,
                    { id ->
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                displayedTagData = "Read: $id"
                                nfcId = id
                                operationComplete = true
                                disableReaderMode()
                                NfcTagManager.clearTag()
                                navController.previousBackStackEntry?.savedStateHandle?.set("filamentId", id)
                                navController.popBackStack()
                            }
                        }
                    },
                    { error ->
                        Log.w("NfcScreen", "Read error: $error")
                        errorMessage = error
                        displayedTagData = "Error: $error"
                        operationComplete = true
                        disableReaderMode()
                        NfcTagManager.clearTag()
                    }
                )
            } else if (mode == FilamentNfcScreenMode.READ) {
                nfcViewModel.readNfcTag(
                    tag,
                    error1,
                    { id ->
                        nfcId = id
                        displayedTagData = "Read: $id"
                        coroutineScope.launch {
                            filamentViewModel.loadFilamentById(nfcId) { success ->
                                operationComplete = true
                                disableReaderMode()
                                NfcTagManager.clearTag()
                                if (success) {
                                    navController.navigate("filamentView/$nfcId")
                                } else {
                                    errorMessage = error2
                                    displayedTagData = "Error: $error2"
                                }
                            }
                        }
                    },
                    { error ->
                        Log.w("NfcScreen", "Read error: $error")
                        errorMessage = error
                        displayedTagData = "Error: $error"
                        operationComplete = true
                        disableReaderMode()
                        NfcTagManager.clearTag()
                    }
                )
            } else if (mode == FilamentNfcScreenMode.UPDATE && filamentId != null) {
                nfcViewModel.updateNfcTag(
                    filamentId,
                    tag,
                    errors,
                    onSuccess = {
                        nfcId = filamentId
                        displayedTagData = "Write: $filamentId (Success)"
                        coroutineScope.launch {
                            operationComplete = true
                            disableReaderMode()
                            NfcTagManager.clearTag()
                            navController.popBackStack()
                        }
                    },
                    onError = { error ->
                        Log.w("NfcScreen", "Write error: $error")
                        errorMessage = error
                        displayedTagData = "Error: $error"
                        operationComplete = true
                        disableReaderMode()
                        NfcTagManager.clearTag()
                    }
                )
            }
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
            BottomNavigationBar(
                navController = navController,
                selectedItem = NavigationItem.INFO
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.nfc),
                contentDescription = stringResource(R.string.nfc),
                tint = SpoolSyncTheme.colors.blackWhite,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )

            Text(
                text = stringResource(R.string.place_device_close_to_tag),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.gray)
            )

            // Display tag data when received
            if (displayedTagData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = displayedTagData,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.light_gray),
                    modifier = Modifier.padding(16.dp)
                )
            }

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
                    colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colorResource(R.color.white),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.submit), color = colorResource(R.color.white))
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