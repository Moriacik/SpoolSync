package com.example.spoolsync.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.spoolsync.FilamentViewModel
import com.example.spoolsync.R
import com.example.spoolsync.ui.FilamentFormMode.ADD
import com.example.spoolsync.ui.FilamentFormMode.EDIT
import com.example.spoolsync.ui.FilamentFormMode.VIEW

enum class FilamentFormMode {
    VIEW, ADD, EDIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentFormScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel,
    mode: FilamentFormMode = VIEW,
    initialFilament: Filament? = null
) {
    android.util.Log.d("FilamentFormScreen", "Initial filament: $initialFilament")
    var filament by remember { mutableStateOf(initialFilament ?: Filament("", "", "", "", "", "", "")) }
    var isEditing by remember { mutableStateOf(mode == EDIT || mode == ADD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (mode) {
                            VIEW -> "Filament Info"
                            ADD -> "Add Filament"
                            EDIT -> "Edit Filament"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Account action */ }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification action */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
            // Photo section
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
                    if (isEditing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { /* Handle photo upload */ }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = "Upload photo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(40.dp)
                            )
                            Text("Click to upload photo")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }

            // Filament Type and Brand (top section)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filament.type.ifEmpty { "PLA" },
                    fontSize = 40.sp,
                    color = if (filament.type.isEmpty()) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = filament.brand.ifEmpty { "Prusament" },
                    fontSize = 20.sp,
                    color = if (filament.brand.isEmpty()) Color.Gray else Color.Black,
                    textAlign = TextAlign.End
                )
            }

            // Form content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(36.dp)
            ) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Filament properties
                if (isEditing) {
                    EditableFilamentForm(filament = filament, onFilamentChange = { filament = it })
                } else {
                    FilamentInfoDisplay(filament = filament)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Note section
                Text(
                    text = "Note",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                if (isEditing) {
                    Text(
                        text = "Optional note about the filament...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "Overall, PLA filament is one of the best filaments to work with: it is not sensitive to temperature changes, has great surface quality, and is ideal for printing complex objects.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Action buttons when in edit mode
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (mode == ADD) {
                                filamentViewModel.addFilament(filament)
                            } else {
                                // Handle update in ViewModel
                            }
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Submit")
                        Text("Submit")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (mode == EDIT) {
                                isEditing = false
                            } else {
                                navController.popBackStack()
                            }
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
        // Color field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.collor),
            value = filament.color,
            onValueChange = { onFilamentChange(filament.copy(color = it)) },
            hint = "Color",
            isEditable = true
        )

        // Weight field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.weight),
            value = filament.weight,
            onValueChange = { onFilamentChange(filament.copy(weight = it)) },
            hint = "Weight",
            isEditable = true
        )

        // Status field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.status),
            value = filament.status,
            onValueChange = { onFilamentChange(filament.copy(status = it)) },
            hint = "Status",
            isEditable = true
        )

        // Expiration date field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.expiration),
            value = filament.expirationDate,
            onValueChange = { onFilamentChange(filament.copy(expirationDate = it)) },
            hint = "Expiration Date",
            isEditable = true
        )
    }
}

@Composable
private fun FilamentInfoDisplay(filament: Filament) {
    Column {
        // Color field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.collor),
            value = filament.color.ifEmpty { "Not specified" },
            hint = "Color",
            isEditable = false
        )

        // Weight field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.weight),
            value = filament.weight.ifEmpty { "Not specified" },
            hint = "Weight",
            isEditable = false
        )

        // Status field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.status),
            value = filament.status.ifEmpty { "Not specified" },
            hint = "Status",
            isEditable = false
        )

        // Expiration date field
        FormFieldWithIcon(
            icon = painterResource(R.drawable.expiration),
            value = filament.expirationDate.ifEmpty { "Not specified" },
            hint = "Expiration Date",
            isEditable = false
        )
    }
}

@Composable
private fun FormFieldWithIcon(
    icon: Painter,
    value: String,
    onValueChange: (String) -> Unit = {},
    hint: String,
    isEditable: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

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

        Column(modifier = Modifier.weight(1f)) {
            if (isEditable) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty() && !isFocused) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    text = if (value.isEmpty()) "Not specified" else value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isEmpty()) Color.Gray else Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}