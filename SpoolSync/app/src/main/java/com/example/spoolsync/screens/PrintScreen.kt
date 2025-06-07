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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spoolsync.R
import androidx.core.net.toUri
import com.example.spoolsync.screens.FilamentFormMode.ADD
import com.example.spoolsync.viewModels.FilamentViewModel

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
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var scannedWeightState by remember { mutableStateOf(scannedWeight) }
    val scannedRoundedWeight = scannedWeightState.toFloatOrNull()?.toInt() ?: 0
    val newWeight = filament?.weight?.minus(scannedRoundedWeight) ?: 0

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
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp, 60.dp)
                            .background(Color.Transparent)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.TopStart),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.TopEnd),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.BottomStart),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.BottomEnd),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.TopStart),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.TopEnd),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.BottomStart),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.BottomEnd),
                            thickness = 4.dp,
                            color = Color.LightGray
                        )
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
                inputType = InputType.TEXT,
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

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Calculate New Weight",
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
                        text = "Actual Weight:",
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
                        text = "Scanned Weight:",
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = scannedWeightState,
                        onValueChange = { scannedWeightState = it },
                        singleLine = true,
                        modifier = Modifier
                            .width(120.dp)
                            .padding(end = 20.dp),
                    )
                    Text(
                        text = "(${scannedRoundedWeight}) g",
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "New Weight:",
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
                    Button(
                        onClick = {
                            filamentViewModel.updateFilamentWeight(
                                filamentId = filament?.id.toString(),
                                newWeight = newWeight
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.light_gray)),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .width(250.dp)
                    ) {
                        Text("Print")
                    }
                }
            }
        }
    }
}