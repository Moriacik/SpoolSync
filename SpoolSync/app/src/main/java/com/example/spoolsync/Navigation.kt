package com.example.spoolsync

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spoolsync.firebase.AuthViewModel
import com.example.spoolsync.ui.FilamentFormMode
import com.example.spoolsync.ui.FilamentFormScreen
import com.example.spoolsync.ui.FilamentsScreen
import com.example.spoolsync.ui.LoginScreen
import com.example.spoolsync.ui.RegisterScreen
import com.example.spoolsync.ui.FilamentNfcScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SpoolSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val filamentViewModel: FilamentViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
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
                filamentViewModel = filamentViewModel,
                mode = FilamentFormMode.ADD,
                initialFilament = null
            )
        }

        composable(
            "filamentView/{filamentId}",
            arguments = listOf(navArgument("filamentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val filamentId = backStackEntry.arguments?.getString("filamentId") ?: ""
            android.util.Log.d("NavigationGraph", "Navigated to FilamentFormScreen with ID: $filamentId")
            FilamentFormScreen(
                navController = navController,
                filamentViewModel = filamentViewModel,
                mode = FilamentFormMode.VIEW,
                initialFilament = filamentViewModel.filaments.find { it.id == filamentId }
            )
        }

        composable("filamentNfc") {
            FilamentNfcScreen(navController)
        }
    }
}