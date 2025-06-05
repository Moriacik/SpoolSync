package com.example.spoolsync.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    navController: NavController
) {
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            navController.navigate("print/${Uri.encode(it.toString())}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Print", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Account action */ }) {
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
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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

            Spacer(modifier = Modifier.height(128.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ){
                Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray, shape = CircleShape)
                ) {
                    Box(
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Text("")
                        }
                    }
                }
            }
        }
    }
}