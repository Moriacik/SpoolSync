package com.example.spoolsync.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.ui.viewModels.FilamentViewModel
import com.example.spoolsync.R
import com.example.spoolsync.data.model.Filament
import com.example.spoolsync.ui.theme.SpoolSyncTheme

/**
 * Hlavná obrazovka pre správu filamentov.
 * Zobrazuje zoznam filamentov a kategórie.
 *
 * @param navController Navigácia v aplikácii.
 * @param filamentViewModel ViewModel pre správu filamentov.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentsScreen(
    navController: NavController,
    filamentViewModel: FilamentViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf("") }
    val filamentStatuses = stringArrayResource(R.array.filament_status_options)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(
                    text = stringResource(R.string.filaments),
                    fontWeight = FontWeight.Bold
                )},
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate("account") })
                    {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.account)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Notification action */ })
                    {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications)
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("settings") })
                    {
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
                containerColor = SpoolSyncTheme.colors.navBar
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
                                tint = SpoolSyncTheme.colors.navBarIconSelected,
                                modifier = Modifier.size(60.dp),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filamentNfcRead") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info),
                                tint = SpoolSyncTheme.colors.navBarIcon,
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
                                tint = SpoolSyncTheme.colors.navBarIcon,
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
                val filteredFilaments = if (selectedCategory.isEmpty()) {
                    filamentViewModel.filaments
                } else {
                    filamentViewModel.filaments.filter { it.status == selectedCategory }
                }

                items(filteredFilaments) { filament ->
                    FilamentItem(filament, navController)
                    Divider(color = SpoolSyncTheme.colors.divider)
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
                items(listOf(R.drawable.ic_filament, R.drawable.ic_box_opened, R.drawable.ic_box_closed, R.drawable.ic_printer)) { imageResource ->
                    CategoryChip(imageResource,
                        filamentStatuses,
                        selectedCategory,
                        onCategorySelected = { category -> selectedCategory = category }
                    )
                }
            }
        }
    }
}

/**
 * Komponent reprezentujúci jednu kategóriu filamentu vo forme čipu.
 *
 * @param imageResource Ikona kategórie.
 * @param filamentStatuses Zoznam statusov filamentov.
 * @param selectedCategory Aktuálne zvolená kategória.
 * @param onCategorySelected Callback pri výbere kategórie.
 */
@Composable
fun CategoryChip(
    imageResource: Int,
    filamentStatuses: Array<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val category = when (imageResource) {
        R.drawable.ic_filament -> ""
        R.drawable.ic_box_opened -> filamentStatuses[0]
        R.drawable.ic_box_closed -> filamentStatuses[1]
        R.drawable.ic_printer -> filamentStatuses[2]
        else -> ""
    }

    val isSelected = category == selectedCategory

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = SpoolSyncTheme.colors.chipBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) SpoolSyncTheme.colors.chipBorder else SpoolSyncTheme.colors.chipBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onCategorySelected(category) },
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
                R.drawable.ic_box_opened -> filamentStatuses[0]
                R.drawable.ic_box_closed -> filamentStatuses[1]
                R.drawable.ic_printer -> filamentStatuses[2]
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}

/**
 * Komponent zobrazujúci jednu položku filamentu v zozname.
 *
 * @param filament Filament, ktorý sa má zobraziť.
 * @param navController Navigácia v aplikácii.
 */
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
                .border(2.dp, SpoolSyncTheme.colors.filamentCircleBorder, CircleShape)
                .background(Color(filament.color.toArgb()), shape = CircleShape)
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
                text = filament.weight.toString(),
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