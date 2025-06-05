package com.example.spoolsync.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintScreen(
    navController: NavController,
    imageUri: String
) {
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var filamentWeight by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }

    LaunchedEffect(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri.toUri())
            selectedImage = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Print", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("filaments") }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification action */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings")
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
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_filament),
                                contentDescription = "Filamenty",
                                tint = Color.Gray,
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
                                contentDescription = "Info",
                                tint = Color.Gray,
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
                                contentDescription = "Tlačiť",
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
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Image Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                        .background(Color.White)
                        .border(2.dp, Color.LightGray)
                ) {
                    selectedImage?.let {
                        Image(bitmap = it.asImageBitmap(), contentDescription = "Selected Image")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filament Information
            Text(
                text = "Filament Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            FormWithIcon(
                icon = painterResource(R.drawable.ic_collor),
                value = "Color: #FFFFFF",
                id = "",
                onValueChange = {},
                inputType = InputType.TEXT,
                navController = navController,
                isEditable = false
            )

            FormWithIcon(
                icon = painterResource(R.drawable.ic_weight),
                value = filamentWeight.ifEmpty { "Weight: Not specified" },
                id = "",
                onValueChange = { filamentWeight = it },
                inputType = InputType.WEIGHT_FIELD,
                navController = navController,
                isEditable = true
            )

            FormWithIcon(
                icon = painterResource(R.drawable.ic_status),
                value = "Status: Open",
                id = "",
                onValueChange = {},
                inputType = InputType.TEXT,
                navController = navController,
                isEditable = false
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Weight Calculation
            Text(
                text = "Calculate New Weight",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = newWeight,
                    onValueChange = { newWeight = it },
                    placeholder = { Text("Enter new weight", color = Color.Gray) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Perform weight calculation */ },
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text("Calculate")
                }
            }
        }
    }
}