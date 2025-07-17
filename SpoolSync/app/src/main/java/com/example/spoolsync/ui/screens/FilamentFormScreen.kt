package com.example.spoolsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.screens.FilamentFormMode.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.spoolsync.data.model.Filament
import com.example.spoolsync.ui.components.FormWithIcon
import com.example.spoolsync.ui.components.InputType
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.FilamentViewModel
import java.time.LocalDate

/**
 * Režimi formulára pre správu filamentu.
 */
enum class FilamentFormMode {
    VIEW, ADD, EDIT
}

/**
 * Obrazovka formulára pre správu filamentu.
 * Umožňuje zobraziť, pridať alebo upraviť informácie o filamente podľa zvoleného režimu.
 *
 * @param navController Navigácia v aplikácii.
 * @param mode Režim formulára.
 * @param initialFilament Počiatočné údaje filamentu (nepovinný).
 * @param filamentViewModel ViewModel pre správu filamentov.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentFormScreen(
    navController: NavController,
    mode: FilamentFormMode = VIEW,
    initialFilament: Filament? = null,
    filamentViewModel: FilamentViewModel
) {
    var defaultColor = colorResource(R.color.white)
    var filament by remember { mutableStateOf(initialFilament ?: Filament("", "", "", 0, "", defaultColor, LocalDate.now(), true, "")) }
    val originalFilament = remember { initialFilament ?: Filament("", "", "", 0, "", defaultColor, LocalDate.now(), true, "") }
    var isEditing by remember { mutableStateOf(mode == EDIT || mode == ADD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = stringResource(R.string.info),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.info), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = SpoolSyncTheme.colors.navBar) {
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
                                tint = SpoolSyncTheme.colors.navBarIcon,
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
                                tint = SpoolSyncTheme.colors.navBarIconSelected,
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
                                tint = SpoolSyncTheme.colors.navBarIcon,
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
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(SpoolSyncTheme.colors.filamentPhotoBorder),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(0.dp, 5.dp, 0.dp, 5.dp)
                        .fillMaxSize()
                        .background(SpoolSyncTheme.colors.filamentPhotoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filament),
                        contentDescription = stringResource(R.string.photo),
                        tint = SpoolSyncTheme.colors.filamentPhotoIcon,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                ) {
                    TextField(
                        value = filament.type,
                        onValueChange = { filament = filament.copy(type = it) },
                        placeholder = { Text(stringResource(R.string.type), color = colorResource(R.color.gray)) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 40.sp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                    )
                    TextField(
                        value = filament.brand,
                        onValueChange = { filament = filament.copy(brand = it) },
                        placeholder = { Text(stringResource(R.string.brand), color = colorResource(R.color.gray)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 20.sp,
                            textAlign = TextAlign.End
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp, 32.dp, 32.dp, 12.dp),
                ) {
                    Text(
                        text = if (filament.type == "") stringResource(R.string.type) else filament.type,
                        fontSize = 40.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )
                    Text(
                        text = if (filament.brand == "") stringResource(R.string.brand) else filament.brand,
                        fontSize = 20.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(36.dp)
            ) {
                if (isEditing) {
                    EditableFilamentForm(filament = filament, onFilamentChange = { filament = it }, navController = navController)
                } else {
                    FilamentInfoDisplay(filament = filament, navController = navController)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = stringResource(R.string.note),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                if (isEditing) {
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .heightIn(min = 80.dp)
                            .background(SpoolSyncTheme.colors.noteBox, RoundedCornerShape(8.dp))
                    ) {
                        TextField(
                            value = filament.note,
                            placeholder = { Text(stringResource(R.string.optional_note), color = colorResource(R.color.gray)) },
                            onValueChange = { filament = filament.copy(note = it) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colorResource(R.color.black)),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                isEditing = false
                                if (mode == ADD) {
                                    filamentViewModel.saveNewFilament(filament)
                                } else {
                                    filamentViewModel.saveExistfilament(filament)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.buttonBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.submit), tint = colorResource(R.color.white))
                            Text(stringResource(R.string.submit), color = colorResource(R.color.white))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                isEditing = false
                                if (mode == ADD) {
                                    navController.navigate("filaments")
                                } else {
                                    filament = originalFilament
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.red)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel), tint = colorResource(R.color.white))
                            Text(stringResource(R.string.cancel), color = colorResource(R.color.white))
                        }
                    }
                } else {
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .heightIn(min = 80.dp)
                            .background(SpoolSyncTheme.colors.noteBox, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = filament.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.black),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    Divider(modifier = Modifier.padding(4.dp, 12.dp))

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Button(
                            onClick = {
                                filamentViewModel.deleteFilament(filament.id)
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .width(280.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.red)
                            )
                        ) {
                            Text(stringResource(R.string.delete_filament), color = colorResource(R.color.white))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Komponent pre editovateľný formulár filamentu.
 * Zobrazuje polia na úpravu vlastností filamentu.
 *
 * @param filament Aktuálne upravovaný filament.
 * @param onFilamentChange Callback pri zmene údajov filamentu.
 * @param navController Navigácia v aplikácii.
 */
@Composable
private fun EditableFilamentForm(
    filament: Filament,
    onFilamentChange: (Filament) -> Unit,
    navController: NavController
) {
    Column {
        FormWithIcon(
            icon = painterResource(R.drawable.ic_collor),
            value = filament.color,
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(color = it)) },
            inputType = InputType.COLOR_PICKER,
            navController = navController,
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_weight),
            value = filament.weight,
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(weight = it)) },
            inputType = InputType.WEIGHT_FIELD,
            navController = navController,
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_status),
            value = filament.status,
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(status = it)) },
            inputType = InputType.DROPDOWN,
            options = stringArrayResource(R.array.filament_status_options).toList(),
            navController = navController,
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_expiration),
            value = filament.expirationDate,
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(expirationDate = it)) },
            inputType = InputType.DATE_PICKER,
            navController = navController,
            isEditable = true
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        FormWithIcon(
            icon = painterResource(R.drawable.ic_nfc),
            value = filament.activeNfc,
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(activeNfc = it)) },
            inputType = InputType.NFC_FIELD,
            navController = navController,
            isEditable = true
        )
    }
}

/**
 * Komponent pre zobrazenie informácií o filamente.
 * Režim iba na čítanie.
 *
 * @param filament Filament, ktorého informácie sa zobrazujú.
 * @param navController Navigácia v aplikácii.
 */
@Composable
private fun FilamentInfoDisplay(
    filament: Filament,
    navController: NavController
) {
    Column {
        FormWithIcon(
            icon = painterResource(R.drawable.ic_collor),
            value = filament.color,
            id = filament.id,
            onValueChange = {},
            inputType = InputType.COLOR_PICKER,
            navController = navController,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_weight),
            value = filament.weight,
            id = filament.id,
            onValueChange = {},
            inputType = InputType.WEIGHT_FIELD,
            navController = navController,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_status),
            value = filament.status.ifEmpty { stringResource(R.string.not_specified) },
            id = filament.id,
            onValueChange = {},
            inputType = InputType.TEXT,
            navController = navController,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.ic_expiration),
            value = filament.expirationDate,
            id = filament.id,
            onValueChange = { },
            inputType = InputType.DATE_PICKER,
            navController = navController,
            isEditable = false
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        FormWithIcon(
            icon = painterResource(R.drawable.ic_nfc),
            value = filament.activeNfc,
            id = filament.id,
            onValueChange = {},
            inputType = InputType.NFC_FIELD,
            navController = navController,
            isEditable = false
        )
    }
}