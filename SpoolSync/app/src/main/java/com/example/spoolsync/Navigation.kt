package com.example.spoolsync

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spoolsync.viewModels.AuthViewModel
import com.example.spoolsync.viewModels.FilamentViewModel
import com.example.spoolsync.screens.FilamentFormMode
import com.example.spoolsync.screens.FilamentFormScreen
import com.example.spoolsync.screens.FilamentsScreen
import com.example.spoolsync.screens.LoginScreen
import com.example.spoolsync.screens.RegisterScreen
import com.example.spoolsync.screens.FilamentNfcScreen
import com.example.spoolsync.screens.FilamentNfcScreenMode
import com.example.spoolsync.screens.OcrScreen
import com.example.spoolsync.screens.PrintScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SpoolSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val filamentViewModel: FilamentViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "filaments",
    ) {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        composable("filaments") {
            filamentViewModel.setUserId(Firebase.auth.currentUser?.uid ?: "")
            FilamentsScreen(
                navController = navController,
                filamentViewModel = filamentViewModel
            )
        }

        composable("filamentAdd") {
            FilamentFormScreen(
                navController = navController,
                mode = FilamentFormMode.ADD,
                initialFilament = null,
                filamentViewModel = filamentViewModel
            )
        }

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

        composable("filamentNfcRead") {
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                FilamentNfcScreenMode.READ,
                null
            )
        }

        composable("filamentNfcReadOcr") {
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                FilamentNfcScreenMode.OCR,
                null
            )
        }

        composable("filamentNfcUpdate/{filamentId}",
            arguments = listOf(navArgument("filamentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val filamentId = backStackEntry.arguments?.getString("filamentId") ?: ""
            FilamentNfcScreen(
                navController,
                filamentViewModel,
                FilamentNfcScreenMode.UPDATE,
                filamentId
            )
        }

        composable("ocr") {
            OcrScreen(navController)
        }

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