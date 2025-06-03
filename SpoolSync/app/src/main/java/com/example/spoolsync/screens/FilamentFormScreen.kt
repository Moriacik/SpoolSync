package com.example.spoolsync.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
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
import com.example.spoolsync.viewModels.FilamentViewModel

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
    var filament by remember { mutableStateOf(initialFilament ?: Filament("", "", "", "", "", "", "", "")) }
    var isEditing by remember { mutableStateOf(mode == EDIT || mode == ADD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.info),
                            contentDescription = "Info",
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Filament Info", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = colorResource(R.color.nav_bar)) {
                NavigationBar(containerColor = Color.Transparent) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filaments") },
                        icon = { Text("Filamenty") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
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
        ) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(0.dp, 5.dp, 0.dp, 5.dp)
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.filament),
                        contentDescription = "Upload photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = filament.type,
                        onValueChange = { filament = filament.copy(type = it) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 40.sp,
                            color = if (filament.type == "") Color.Gray else Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    BasicTextField(
                        value = filament.brand,
                        onValueChange = { filament = filament.copy(brand = it) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 20.sp,
                            color = if (filament.brand == "") Color.Gray else Color.Black,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = filament.type,
                        fontSize = 40.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = filament.brand,
                        fontSize = 20.sp,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(36.dp)
            ) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                if (isEditing) {
                    EditableFilamentForm(filament = filament, onFilamentChange = { filament = it })
                } else {
                    FilamentInfoDisplay(filament = filament)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Note",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                if (isEditing) {
                    var text = "Optional note about the filament..."
                    if (!filament.note.isEmpty()) {
                        text = filament.note
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = { filament = filament.copy(note = it) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        text = filament.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            if (isEditing) {
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Submit")
                        Text("Submit")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableFilamentForm(
    filament: Filament,
    onFilamentChange: (Filament) -> Unit
) {
    Column {
        FormWithIcon(
            icon = painterResource(R.drawable.collor),
            value = filament.color,
            onValueChange = { onFilamentChange(filament.copy(color = it)) },
            inputType = InputType.COLOR_PICKER,
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.weight),
            value = filament.weight,
            onValueChange = { onFilamentChange(filament.copy(weight = it)) },
            inputType = InputType.WEIGHT_FIELD,
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.status),
            value = filament.status,
            onValueChange = { onFilamentChange(filament.copy(status = it)) },
            inputType = InputType.DROPDOWN,
            options = listOf("Zatvorene", "Otvorene", "V tlačiarni"),
            isEditable = true
        )

        FormWithIcon(
            icon = painterResource(R.drawable.expiration),
            value = filament.expirationDate,
            onValueChange = { onFilamentChange(filament.copy(expirationDate = it)) },
            inputType = InputType.DROPDOWN,
            options = listOf("Týždeň", "Mesiac", "Rok"),
            isEditable = true
        )
    }
}

@Composable
private fun FilamentInfoDisplay(filament: Filament) {
    Column {
        FormWithIcon(
            icon = painterResource(R.drawable.collor),
            value = filament.color.ifEmpty { "Not specified" },
            onValueChange = {},
            inputType = InputType.COLOR_PICKER,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.weight),
            value = filament.weight.ifEmpty { "Not specified" },
            onValueChange = {},
            inputType = InputType.WEIGHT_FIELD,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.status),
            value = filament.status.ifEmpty { "Not specified" },
            onValueChange = {},
            inputType = InputType.TEXT,
            isEditable = false
        )

        FormWithIcon(
            icon = painterResource(R.drawable.expiration),
            value = filament.expirationDate.ifEmpty { "Not specified" },
            onValueChange = {},
            inputType = InputType.TEXT,
            isEditable = false
        )
    }
}

@Composable
fun FormWithIcon(
    icon: Painter,
    value: String,
    onValueChange: (String) -> Unit,
    inputType: InputType,
    options: List<String> = emptyList(),
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
            contentDescription = "property icon",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        if (isEditable) {
            Column(modifier = Modifier.weight(1f)) {
                when (inputType) {
                    InputType.TEXT -> BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    InputType.WEIGHT_FIELD -> WeightInputField(
                        weight = value,
                        onWeightChange = onValueChange
                    )

                    InputType.DROPDOWN -> DropdownField(
                        options = options,
                        selectedOption = value,
                        onOptionSelected = onValueChange
                    )

                    InputType.COLOR_PICKER -> {
                        val defaultColor = Color.White
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(value))
                        } catch (e: Exception) {
                            defaultColor
                        }
                        ColorPicker(
                            selectedColor = parsedColor,
                            onColorSelected = { onValueChange("#${Integer.toHexString(it.toArgb())}") }
                        )
                    }
                }
            }
        } else {
            when (inputType) {
                InputType.COLOR_PICKER ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(android.graphics.Color.parseColor(value)), CircleShape)
                            .border(2.dp, Color.LightGray, CircleShape)
                    )

                InputType.WEIGHT_FIELD -> Text(
                    text = "$value g",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )

                else -> Text(
                    text = value.ifEmpty { "Not specified" },
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

enum class InputType {
    TEXT, WEIGHT_FIELD, DROPDOWN, COLOR_PICKER
}

@Composable
fun WeightInputField(
    weight: String,
    onWeightChange: (String) -> Unit
) {
    var inputWeight by remember { mutableStateOf(weight) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = inputWeight,
            onValueChange = { newValue ->
                val numericValue = newValue.filter { it.isDigit() }
                inputWeight = numericValue
                onWeightChange(numericValue)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "g",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
            modifier = Modifier.padding(end = 50.dp)
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
                    .padding(end = 50.dp),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
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
            .border(2.dp, Color.LightGray, CircleShape)
            .clickable { showColorPicker = true }
    ) {
        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                confirmButton = {
                    TextButton(onClick = { showColorPicker = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Select Color") },
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