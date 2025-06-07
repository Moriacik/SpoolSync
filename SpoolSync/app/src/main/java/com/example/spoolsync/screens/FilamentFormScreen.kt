package com.example.spoolsync.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.screens.FilamentFormMode.*
import com.godaddy.android.colorpicker.ClassicColorPicker
import kotlin.Boolean
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.spoolsync.viewModels.FilamentViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.toString

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
    var defaultColor = colorResource(R.color.default_color)
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
            BottomAppBar(containerColor = colorResource(R.color.nav_bar)) {
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
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.light_gray)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(0.dp, 5.dp, 0.dp, 5.dp)
                        .fillMaxSize()
                        .background(colorResource(R.color.white)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_filament),
                        contentDescription = stringResource(R.string.photo),
                        contentScale = ContentScale.Fit,
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
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black)
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
                            focusedTextColor = colorResource(R.color.black),
                            unfocusedTextColor = colorResource(R.color.black)
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
                        color = colorResource(R.color.black),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )
                    Text(
                        text = if (filament.brand == "") stringResource(R.string.brand) else filament.brand,
                        fontSize = 20.sp,
                        color = colorResource(R.color.black),
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
                            .background(colorResource(R.color.light_gray), RoundedCornerShape(8.dp))
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
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = colorResource(R.color.black),
                                unfocusedTextColor = colorResource(R.color.black)
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
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.light_gray)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.submit))
                            Text(stringResource(R.string.submit))
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
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                            Text(stringResource(R.string.cancel))
                        }
                    }
                } else {
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .heightIn(min = 80.dp)
                            .background(colorResource(R.color.light_gray), RoundedCornerShape(8.dp))
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
                                containerColor = colorResource(R.color.red),
                                contentColor = colorResource(R.color.white)
                            )
                        ) {
                            Text(stringResource(R.string.delete_filament))
                        }
                    }
                }
            }
        }
    }
}

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

@Composable
fun <T> FormWithIcon(
    icon: Painter,
    value: T,
    id: String,
    onValueChange: (T) -> Unit,
    inputType: InputType,
    options: List<String> = emptyList(),
    navController: NavController,
    isEditable: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = stringResource(R.string.filament_status),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        if (isEditable) {
            Column(modifier = Modifier.weight(1f)) {
                when (inputType) {
                    InputType.TEXT -> BasicTextField(
                        value = value.toString(),
                        onValueChange = { onValueChange(it as T) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.black)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    InputType.WEIGHT_FIELD -> WeightInputField(
                        weight = value.toString(),
                        onWeightChange = { onValueChange(it as T) }
                    )

                    InputType.DROPDOWN -> DropdownField(
                        options = options,
                        selectedOption = value.toString(),
                        onOptionSelected = { onValueChange(it as T) }
                    )

                    InputType.DATE_PICKER -> DatePicker(
                        selectedDate = value as LocalDate,
                        onDateSelected = { onValueChange(it as T) }
                    )

                    InputType.COLOR_PICKER -> ColorPicker(
                        selectedColor = value as Color,
                        onColorSelected = { onValueChange(it as T) }
                    )

                    InputType.NFC_FIELD -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (value != true) stringResource(R.string.scanned) else stringResource(R.string.not_scanned),
                                style = MaterialTheme.typography.bodyLarge.copy(color = if (value != true) colorResource(R.color.green) else colorResource(R.color.red)),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp)
                            )
                            Button(
                                onClick = {
                                    navController.navigate("filamentNfcUpdate/${id}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.light_gray)),
                                modifier = Modifier
                                    .padding(end = 10.dp)
                            ) {
                                Text(stringResource(R.string.update_tag))
                            }
                        }
                    }
                }
            }
        } else {
            when (inputType) {
                InputType.COLOR_PICKER -> Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(value as Color, CircleShape)
                        .border(2.dp, colorResource(R.color.dark_gray), CircleShape)
                )

                InputType.WEIGHT_FIELD -> Text(
                    text = "$value g",
                    style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.black)),
                    modifier = Modifier.fillMaxWidth()
                )

                InputType.NFC_FIELD -> Text(
                    text = if (value != true) stringResource(R.string.scanned) else stringResource(R.string.not_scanned),
                    style = MaterialTheme.typography.bodyLarge.copy(color = if (value != true) colorResource(R.color.green) else colorResource(R.color.red)),
                    modifier = Modifier.fillMaxWidth()
                )

                else -> Text(
                    text = value.toString().ifEmpty { stringResource(R.string.not_specified) },
                    style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.black)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

enum class InputType {
    TEXT, WEIGHT_FIELD, DROPDOWN, DATE_PICKER, COLOR_PICKER, NFC_FIELD
}

@Composable
fun WeightInputField(
    weight: String,
    onWeightChange: (Int) -> Unit
) {
    var inputWeight by remember { mutableStateOf(weight) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp)
            .background(colorResource(R.color.light_gray), RoundedCornerShape(10.dp))
    ) {
        TextField(
            value = inputWeight,
            placeholder = { Text(stringResource(R.string.weight_in_grams), color = colorResource(R.color.gray)) },
            onValueChange = { newValue ->
                val numericValue = newValue.filter { it.isDigit() }
                inputWeight = numericValue
                onWeightChange(numericValue.toIntOrNull() ?: 0)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.black)),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = colorResource(R.color.black),
                unfocusedTextColor = colorResource(R.color.black)
            ),
            modifier = Modifier
                .weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "g",
            style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.gray)),
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(end = 10.dp),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = colorResource(R.color.light_gray),
                    unfocusedContainerColor = colorResource(R.color.light_gray)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .border(1.dp, colorResource(R.color.light_gray))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(colorResource(R.color.white))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var color = HsvColor.from(selectedColor)

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(selectedColor, CircleShape)
            .border(2.dp, Color.DarkGray, CircleShape)
            .clickable { showColorPicker = true }
    ) {
        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text(stringResource(R.string.submit))
                    }
                },
                title = { Text(stringResource(R.string.select_color)) },
                text = {
                    Column {
                        ClassicColorPicker(
                            color = color,
                            onColorChanged = { newColor ->
                                color = newColor
                                onColorSelected(Color(newColor.toColorInt()))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(end = 12.dp)
            .background(colorResource(R.color.light_gray), RoundedCornerShape(10.dp))
    ){
        Text(
            text = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val newDate = Instant.ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                onDateSelected(newDate)
                            }
                            showDatePicker = false
                        }
                    ) { Text(stringResource(R.string.submit)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}