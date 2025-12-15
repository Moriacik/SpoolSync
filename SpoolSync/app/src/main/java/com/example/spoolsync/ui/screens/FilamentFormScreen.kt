package com.example.spoolsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.screens.FilamentFormMode.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.spoolsync.data.model.Filament
import com.example.spoolsync.ui.components.BottomNavigationBar
import com.example.spoolsync.ui.components.FormWithIcon
import com.example.spoolsync.ui.components.InputType
import com.example.spoolsync.ui.components.NavigationItem
import com.example.spoolsync.ui.components.ShareFilamentDialog
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.FilamentViewModel
import java.time.LocalDate

/**
 * Režimi formulára pre správu filamentu.
 */
enum class FilamentFormMode {
    VIEW, ADD, EDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentFormScreen(
    navController: NavController,
    mode: FilamentFormMode = VIEW,
    initialFilament: Filament? = null,
    filamentViewModel: FilamentViewModel
) {
    val sessionsState by filamentViewModel.sessions.collectAsState()
    var filament by remember { mutableStateOf(initialFilament ?: Filament("", "", "", 0, "", "#FFFFFF", LocalDate.now().toString(), false, "")) }
    val originalFilament = remember { initialFilament ?: Filament("", "", "", 0, "", "#FFFFFF", LocalDate.now().toString(), false, "") }
    var isEditing by remember { mutableStateOf(mode == EDIT || mode == ADD) }
    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FilamentFormTopBar(isEditing) { isEditing = true }
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
                .padding(innerPadding)
        ) {
            FilamentHeader()

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (isEditing) {
                EditMode(
                    filament = filament,
                    onFilamentChange = { filament = it },
                    originalFilament = originalFilament,
                    mode = mode,
                    navController = navController,
                    filamentViewModel = filamentViewModel,
                    onEditingDone = { isEditing = false }
                )
            } else {
                ViewMode(
                    filament = filament,
                    navController = navController,
                    filamentViewModel = filamentViewModel,
                    sessionsState = sessionsState,
                    showShareDialog = showShareDialog,
                    onShareClick = { showShareDialog = true },
                    onShareDone = {
                        showShareDialog = false
                        navController.navigate("filaments") {
                            popUpTo("filamentView/{filamentId}") { inclusive = true }
                        }
                    },
                    onDeleteClick = {
                        filamentViewModel.deleteFilament(filament.id)
                        navController.popBackStack()
                    }
                )
            }

            if (showShareDialog) {
                ShareFilamentDialog(
                    filament = filament,
                    onDismiss = { showShareDialog = false },
                    onConfirm = { sessionId ->
                        filamentViewModel.moveFilamentToSession(
                            filamentId = filament.id,
                            sessionId = sessionId,
                            onSuccess = {
                                navController.navigate("filaments") {
                                    popUpTo("filamentView/{filamentId}") { inclusive = true }
                                }
                            }
                        )
                        showShareDialog = false
                    },
                    sessions = sessionsState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilamentFormTopBar(isEditing: Boolean, onEditClick: () -> Unit) {
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
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
            }
        }
    )
}

@Composable
private fun FilamentHeader() {
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .background(SpoolSyncTheme.colors.lightGrayDarkGray),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp, 5.dp, 0.dp, 5.dp)
                .fillMaxSize()
                .background(SpoolSyncTheme.colors.whiteDarkerGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filament),
                contentDescription = stringResource(R.string.photo),
                tint = SpoolSyncTheme.colors.blackWhite,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

@Composable
private fun EditMode(
    filament: Filament,
    onFilamentChange: (Filament) -> Unit,
    originalFilament: Filament,
    mode: FilamentFormMode,
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    onEditingDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(36.dp)
    ) {
        EditableFilamentHeader(filament, onFilamentChange)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        EditableFilamentForm(
            filament = filament,
            onFilamentChange = onFilamentChange,
            navController = navController
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        EditableNoteSection(filament, onFilamentChange)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        EditActionButtons(
            filament = filament,
            originalFilament = originalFilament,
            mode = mode,
            navController = navController,
            filamentViewModel = filamentViewModel,
            onEditingDone = onEditingDone
        )
    }
}

@Composable
private fun EditableFilamentHeader(filament: Filament, onFilamentChange: (Filament) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 0.dp)
    ) {
        TextField(
            value = filament.type,
            onValueChange = { onFilamentChange(filament.copy(type = it)) },
            placeholder = { Text(stringResource(R.string.type), color = colorResource(R.color.gray)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 40.sp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier.weight(1f)
        )
        TextField(
            value = filament.brand,
            onValueChange = { onFilamentChange(filament.copy(brand = it)) },
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
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EditableNoteSection(filament: Filament, onFilamentChange: (Filament) -> Unit) {
    Text(
        text = stringResource(R.string.note),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 80.dp)
            .background(SpoolSyncTheme.colors.lightGrayGray, RoundedCornerShape(8.dp))
    ) {
        TextField(
            value = filament.note,
            placeholder = { Text(stringResource(R.string.optional_note), color = colorResource(R.color.gray)) },
            onValueChange = { onFilamentChange(filament.copy(note = it)) },
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
}

@Composable
private fun EditActionButtons(
    filament: Filament,
    originalFilament: Filament,
    mode: FilamentFormMode,
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    onEditingDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                onEditingDone()
                if (mode == ADD) {
                    filamentViewModel.saveNewFilament(filament)
                } else {
                    filamentViewModel.saveExistfilament(filament)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.submit), tint = colorResource(R.color.white))
            Text(stringResource(R.string.submit), color = colorResource(R.color.white))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = {
                onEditingDone()
                if (mode == ADD) {
                    navController.navigate("filaments")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.red)),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel), tint = colorResource(R.color.white))
            Text(stringResource(R.string.cancel), color = colorResource(R.color.white))
        }
    }
}

@Composable
private fun ViewMode(
    filament: Filament,
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    sessionsState: Map<String, String>,
    showShareDialog: Boolean,
    onShareClick: () -> Unit,
    onShareDone: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(36.dp)
    ) {
        ViewFilamentHeader(filament)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        FilamentInfoDisplay(filament = filament, navController = navController)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        ShareButton(onShareClick)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        ViewNoteSection(filament)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        DeleteButton(onDeleteClick)
    }
}

@Composable
private fun ViewFilamentHeader(filament: Filament) {
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

@Composable
private fun ShareButton(onShareClick: () -> Unit) {
    Button(
        onClick = onShareClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SpoolSyncTheme.colors.lightGrayGray
        )
    ) {
        Text(stringResource(R.string.share), color = colorResource(R.color.white))
    }
}

@Composable
private fun ViewNoteSection(filament: Filament) {
    Text(
        text = stringResource(R.string.note),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 80.dp)
            .background(SpoolSyncTheme.colors.lightGrayGray, RoundedCornerShape(8.dp))
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
}

@Composable
private fun DeleteButton(onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onDeleteClick,
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
            onValueChange = { color -> onFilamentChange(filament.copy(colorHex = String.format("#%08X", (color as Color).toArgb()))) },
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
            value = LocalDate.parse(filament.expirationDate),
            id = filament.id,
            onValueChange = { onFilamentChange(filament.copy(expirationDate = it.toString())) },
            inputType = InputType.DATE_PICKER,
            navController = navController,
            isEditable = true
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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
            value = LocalDate.parse(filament.expirationDate),
            id = filament.id,
            onValueChange = { },
            inputType = InputType.DATE_PICKER,
            navController = navController,
            isEditable = false
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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