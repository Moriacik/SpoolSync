package com.example.spoolsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Typy vstupov, ktoré môžu byť použité v komponente FormWithIcon.
 * Každý typ reprezentuje iný spôsob zadávania a práce s údajmi.
 */
enum class InputType {
    TEXT, WEIGHT_FIELD, DROPDOWN, DATE_PICKER, COLOR_PICKER, NFC_FIELD
}

/**
 * Komponent pre formulár s ikonou, ktorý umožňuje zadávanie rôznych typov údajov.
 *
 * @param icon Ikona, ktorá sa zobrazí vedľa formulára.
 * @param value Aktuálna hodnota poľa formuláur.
 * @param id Identifikátor filamentu.
 * @param onValueChange Funkcia, ktorá sa zavolá pri zmene hodnoty formulára.
 * @param inputType Typ vstupu.
 * @param options Možnosti pre dropdown menu.
 * @param navController Navigačný kontrolér pre navigáciu v aplikácii.
 * @param isEditable Určuje, či je formulár editovateľný.
 */
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

/**
 * Komponent pre zadávanie hmotnosti.
 *
 * @param weight Aktuálna hmotnosť vo forme reťazca.
 * @param onWeightChange Funkcia, ktorá sa zavolá pri zmene hmotnosti.
 */
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

/**
 * Komponent pre výber z viacerých možností.
 *
 * @param options Zoznam možností na výber.
 * @param selectedOption Aktuálne vybraná možnosť.
 * @param onOptionSelected Funkcia, ktorá sa zavolá pri výbere novej možnosti.
 */
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

/**
 * Komponent pre výber farby.
 *
 * @param selectedColor Aktuálne vybraná farba.
 * @param onColorSelected Funkcia, ktorá sa zavolá pri výbere novej farby.
 */
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

/**
 * Komponent pre výber dátumu.
 *
 * @param selectedDate Aktuálne vybraný dátum.
 * @param onDateSelected Funkcia, ktorá sa zavolá pri výbere nového dátumu.
 */
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

/**
 * Dekoratívny komponent vytvárajúci zameriavanie pre OCR.
 *
 * @param modifier Modifier pre úpravu vzhľadu komponentu.
 * @param boxWidth Šírka boxu s rohmi.
 * @param boxHeight Výška boxu s rohmi.
 * @param dividerLength Dĺžka jednotlivých deliacich čiar.
 * @param dividerThickness Hrúbka deliacich čiar.
 * @param dividerColor Farba deliacich čiar.
 */
@Composable
fun DecorativeCornerDividers(
    modifier: Modifier = Modifier,
    boxWidth: Dp = 100.dp,
    boxHeight: Dp = 60.dp,
    dividerLength: Dp = 20.dp,
    dividerThickness: Dp = 4.dp,
    dividerColor: Color = colorResource(R.color.light_gray)
) {
    Box(
        modifier = modifier
            .size(boxWidth, boxHeight)
    ) {
        // Horizontálne deliace čiary
        HorizontalDivider(
            modifier = Modifier
                .width(dividerLength)
                .align(Alignment.TopStart),
            thickness = dividerThickness,
            color = dividerColor
        )
        HorizontalDivider(
            modifier = Modifier
                .width(dividerLength)
                .align(Alignment.TopEnd),
            thickness = dividerThickness,
            color = dividerColor
        )
        HorizontalDivider(
            modifier = Modifier
                .width(dividerLength)
                .align(Alignment.BottomStart),
            thickness = dividerThickness,
            color = dividerColor
        )
        HorizontalDivider(
            modifier = Modifier
                .width(dividerLength)
                .align(Alignment.BottomEnd),
            thickness = dividerThickness,
            color = dividerColor
        )

        // Vertikálne deliace čiary v rohoch
        VerticalDivider(
            modifier = Modifier
                .height(dividerLength)
                .align(Alignment.TopStart),
            thickness = dividerThickness,
            color = dividerColor
        )
        VerticalDivider(
            modifier = Modifier
                .height(dividerLength)
                .align(Alignment.TopEnd),
            thickness = dividerThickness,
            color = dividerColor
        )
        VerticalDivider(
            modifier = Modifier
                .height(dividerLength)
                .align(Alignment.BottomStart),
            thickness = dividerThickness,
            color = dividerColor
        )
        VerticalDivider(
            modifier = Modifier
                .height(dividerLength)
                .align(Alignment.BottomEnd),
            thickness = dividerThickness,
            color = dividerColor
        )
    }
}