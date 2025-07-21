package com.example.spoolsync.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import androidx.core.net.toUri
import com.example.spoolsync.data.model.Filament
import com.example.spoolsync.ui.components.DecorativeCornerDividers
import com.example.spoolsync.ui.components.FormWithIcon
import com.example.spoolsync.ui.components.InputType
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.FilamentViewModel

/**
 * Obrazovka pre aktualizáciu hmotnosti filamentu po tlači.
 * Zobrazuje vybraný obrázok, informácie o filamente a umožňuje zmeniť naskenovanú hmotnosť.
 * Po potvrdení aktualizuje hmotnosť filamentu a naviguje späť na zoznam filamentov.
 *
 * @param navController Navigácia v aplikácii.
 * @param filamentViewModel ViewModel pre správu filamentov.
 * @param imageUri URI obrázka naskenovaného filamentu.
 * @param filament Filament, ktorého hmotnosť sa aktualizuje.
 * @param scannedWeight Naskenovaná hmotnosť filamentu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    imageUri: String,
    filament: Filament? = null,
    scannedWeight: String
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var scannedWeightState by remember { mutableStateOf(scannedWeight) }
    val scannedRoundedWeight = scannedWeightState.toFloatOrNull()?.toInt() ?: 0
    val newWeight = filament?.weight?.minus(scannedRoundedWeight) ?: 0
    val error1 = stringResource(R.string.negative_weight)

    LaunchedEffect(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri.toUri())
            selectedImage = BitmapFactory.decodeStream(inputStream)
        } catch (_: Exception) {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_printer),
                            contentDescription = stringResource(R.string.print),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.print), fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SpoolSyncTheme.colors.lighterGrayDarkerGray
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
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filamentNfcRead") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info),
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {  },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_printer),
                                contentDescription = stringResource(R.string.print),
                                tint = SpoolSyncTheme.colors.blackWhite,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                        .background(Color.White)
                        .border(2.dp, Color.LightGray)
                ) {
                    selectedImage?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = stringResource(R.string.selected_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp, 60.dp)
                            .background(Color.Transparent)
                    ) {
                        DecorativeCornerDividers()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filament Information
            Text(
                text = stringResource(R.string.filament_information),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = filament?.type ?: "",
                    fontSize = 40.sp,
                    modifier = Modifier
                            .weight(1f)
                )
                Text(
                    text = filament?.brand ?: "",
                    fontSize = 20.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                )
            }

            FormWithIcon(
                icon = painterResource(R.drawable.ic_collor),
                value = filament?.color,
                id = "",
                onValueChange = {},
                inputType = InputType.COLOR_PICKER,
                navController = navController,
                isEditable = false
            )

            FormWithIcon(
                icon = painterResource(R.drawable.ic_status),
                value = filament?.status,
                id = "",
                onValueChange = {},
                inputType = InputType.TEXT,
                navController = navController,
                isEditable = false
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.calculate_new_weight),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.actual_weight),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${filament?.weight} g",
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.scanned_weight),
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = scannedWeightState,
                        onValueChange = { scannedWeightState = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(120.dp)
                            .padding(end = 20.dp),
                    )
                    Text(
                        text = "(${scannedRoundedWeight}) g",
                        color = colorResource(R.color.gray),
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_weight),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$newWeight g",
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = colorResource(R.color.red),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            val weight = scannedWeightState.toIntOrNull()
                            if (weight == null || weight <= 0) {
                                errorMessage = error1
                            } else {
                                filamentViewModel.updateFilamentWeight(
                                    filamentId = filament?.id.toString(),
                                    newWeight = newWeight
                                )
                                navController.navigate("filaments") {
                                    popUpTo("filaments") { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .width(250.dp)
                    ) {
                        Text(stringResource(R.string.print))
                    }
                }
            }
        }
    }
}