package com.example.spoolsync.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spoolsync.ui.viewModels.AuthViewModel
import com.example.spoolsync.ui.viewModels.FilamentViewModel
import com.example.spoolsync.ui.screens.FilamentFormMode
import com.example.spoolsync.ui.screens.FilamentFormScreen
import com.example.spoolsync.ui.screens.FilamentsScreen
import com.example.spoolsync.ui.screens.LoginScreen
import com.example.spoolsync.ui.screens.RegisterScreen
import com.example.spoolsync.ui.screens.FilamentNfcScreen
import com.example.spoolsync.ui.screens.FilamentNfcScreenMode
import com.example.spoolsync.ui.screens.OcrScreen
import com.example.spoolsync.ui.screens.PrintScreen
import com.example.spoolsync.ui.viewModels.NfcViewModel
import com.example.spoolsync.ui.viewModels.OcrViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Hlavná navigačná funkcia aplikácie SpoolSync.
 * Definuje všetky navigačné trasy a prepája ich s príslušnými obrazovkami a view modelmi.
 */
@Composable
fun SpoolSyncApp(startDestination: String) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val filamentViewModel: FilamentViewModel = viewModel()
    val ocrViewModel: OcrViewModel = viewModel()
    val nfcViewModel: NfcViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Prihlasovanie
        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        // Registrácia
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        // Zoznam filamentov
        composable("filaments") {
            filamentViewModel.setUserId(Firebase.auth.currentUser?.uid ?: "")
            FilamentsScreen(
                navController = navController,
                filamentViewModel = filamentViewModel
            )
        }

        // Pridanie nového filamentu
        composable("filamentAdd") {
            FilamentFormScreen(
                navController = navController,
                mode = FilamentFormMode.ADD,
                initialFilament = null,
                filamentViewModel = filamentViewModel
            )
        }

        // Detail filamentu podľa ID
        composable(
            "filamentView/{filamentId}",
            arguments = listOf(navArgument("filamentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val filamentId = backStackEntry.arguments?.getString("filamentId") ?: ""
            FilamentFormScreen(
                navController = navController,
                mode = FilamentFormMode.VIEW,
                initialFilament = filamentViewModel.filaments.find { it.id == filamentId },
                filamentViewModel = filamentViewModel
            )
        }

        // Čítanie filamentu cez NFC
        composable("filamentNfcRead") {
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                nfcViewModel,
                FilamentNfcScreenMode.READ,
                null
            )
        }

        // Čítanie filamentu cez NFC pre OCR
        composable("filamentNfcReadOcr") {
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                nfcViewModel,
                FilamentNfcScreenMode.OCR,
                null
            )
        }

        // Aktualizácia NFC podľa ID
        composable("filamentNfcUpdate/{filamentId}",
            arguments = listOf(navArgument("filamentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val filamentId = backStackEntry.arguments?.getString("filamentId") ?: ""
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                nfcViewModel,
                FilamentNfcScreenMode.UPDATE,
                filamentId
            )
        }

        // OCR skenovanie
        composable("ocr") {
            OcrScreen(navController, ocrViewModel)
        }

        // Tlač filamentu
        composable("print/{imageUri}/{filamentId}/{scannedWeight}",
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("filamentId") { type = NavType.StringType },
                navArgument("scannedWeight") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val filamentId = backStackEntry.arguments?.getString("filamentId") ?: ""
            val scannedWeight = backStackEntry.arguments?.getString("scannedWeight") ?: ""
            PrintScreen(
                navController,
                filamentViewModel,
                imageUri,
                filament = filamentViewModel.filaments.find { it.id == filamentId },
                scannedWeight
            )
        }
    }
}