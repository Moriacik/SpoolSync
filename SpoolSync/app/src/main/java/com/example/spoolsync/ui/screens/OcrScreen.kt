package com.example.spoolsync.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.viewModels.OcrViewModel
import kotlinx.coroutines.launch
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    navController: NavController,
    ocrViewModel: OcrViewModel
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            coroutineScope.launch {
                val scannedText = ocrViewModel.recognizeTextFromCroppedImage(context, it)
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("selectedImageUri", it.toString())
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("scannedWeight", scannedText)

                navController.navigate("filamentNfcReadOcr")
            }
        }
    }

    val filamentIdResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("filamentId")
        ?.value

    val savedImageUri = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selectedImageUri")
    selectedImageUri = savedImageUri?.let { Uri.parse(it) }

    val savedScannedWeight = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("scannedWeight")

    LaunchedEffect(filamentIdResult) {
        if (filamentIdResult != null && selectedImageUri != null) {
            navController.navigate("print/${Uri.encode(selectedImageUri.toString())}/$filamentIdResult/$savedScannedWeight") {
                popUpTo("filamentNfcRead") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_printer),
                            contentDescription = stringResource(R.string.print),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.print), fontWeight = FontWeight.Bold)
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
                                contentDescription = stringResource(R.string.filaments),
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
                        selected = true,
                        onClick = {  },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_printer),
                                contentDescription = stringResource(R.string.print),
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
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
                        .border(2.dp, colorResource(R.color.light_gray))
                ) {
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
                            color = colorResource(R.color.light_gray)
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.TopEnd),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.BottomStart),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .width(20.dp)
                                .align(Alignment.BottomEnd),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.TopStart),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.TopEnd),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.BottomStart),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp)
                                .align(Alignment.BottomEnd),
                            thickness = 4.dp,
                            color = colorResource(R.color.light_gray)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(128.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray, shape = CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
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