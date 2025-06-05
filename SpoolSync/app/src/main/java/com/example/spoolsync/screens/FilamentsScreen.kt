package com.example.spoolsync.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.viewModels.FilamentViewModel
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
                    text = stringResource(R.string.filaments),
                    fontWeight = FontWeight.Bold
                )},
                navigationIcon = {
                    IconButton(onClick = { /* Account action */ }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.account)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification action */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications)
                        )
                    }
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
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
                            Icon(
                                painter = painterResource(R.drawable.ic_filament),
                                contentDescription = stringResource(R.string.filaments),
                                modifier = Modifier.size(56.dp),
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
                                contentDescription = stringResource(R.string.info),
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("ocr") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_printer),
                                contentDescription = stringResource(R.string.print),
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
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
            Row {
                IconButton(onClick = { navController.navigate("filamentAdd") }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add),
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
                text = stringResource(R.string.categories),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )

            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf(R.drawable.ic_filament, R.drawable.ic_printer , R.drawable.ic_box_closed, R.drawable.ic_box_opened)) { imageResource ->
                    CategoryChip(imageResource = imageResource)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(imageResource: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(color = colorResource(R.color.light_gray), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(imageResource),
                contentDescription = stringResource(R.string.filaments),
                modifier = Modifier.size(70.dp)
            )
        }

        Text(
            text = when (imageResource) {
                R.drawable.ic_filament -> stringResource(R.string.all)
                R.drawable.ic_printer -> stringArrayResource(R.array.filament_status_options)[1]
                R.drawable.ic_box_closed -> stringArrayResource(R.array.filament_status_options)[2]
                R.drawable.ic_box_opened -> stringArrayResource(R.array.filament_status_options)[3]
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Start)
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
                .size(50.dp)
                .border(2.dp, Color.DarkGray, CircleShape)
                .background(Color(filament.color.toColorInt()), shape = CircleShape)
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
    val expirationDate: String,
    val activeNfc: String,
    val note: String
)