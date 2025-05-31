package com.example.spoolsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.FilamentViewModel
import com.example.spoolsync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentsScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(
                    text = "Filaments",
                    fontWeight = FontWeight.Bold
                )},
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
                        selected = true,
                        onClick = { },
                        icon = {
                            // Icon(
                            //     painter = painterResource(R.drawable.ic_filament),
                            //     contentDescription = "Filamenty"
                            // )
                            Text("Filamenty")
                        }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filamentNfc") },
                        icon = {
                            // Icon(
                            //     painter = painterResource(R.drawable.ic_info),
                            //     contentDescription = "Info"
                            // )
                            Text("Info")
                        }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { /* Navigácia k tlači */ },
                        icon = {
                            // Icon(
                            //     painter = painterResource(R.drawable.ic_print),
                            //     contentDescription = "Tlačiť"
                            // )
                            Text("Tlačiť")
                        }
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
            Row {
                IconButton(onClick = { navController.navigate("filamentAdd") }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filamentViewModel.filaments) { filament ->
                    FilamentItem(filament, navController)
                    Divider(color = Color.LightGray)
                }
            }

            Text(
                text = "Kategórie",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )

            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("All", "Prusa MK3S", "Bambulab A1", "Packed", "Unpacked")) { category ->
                    CategoryChip(category = category)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FilamentItem(filament: Filament, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("filamentView/${filament.id}") },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color.Black, CircleShape)
                .background(Color.Red, shape = CircleShape)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp)
        ) {
            Text(
                text = filament.type,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = filament.brand,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = filament.weight,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = filament.status,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class Filament(
    val id: String,
    val type: String,
    val brand: String,
    val weight: String,
    val status: String,
    val color: String,
    val expirationDate: String
)