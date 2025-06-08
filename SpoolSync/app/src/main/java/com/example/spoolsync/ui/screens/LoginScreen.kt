package com.example.spoolsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.viewModels.AuthViewModel

/**
 * Obrazovka pre prihlásenie používateľa.
 * Umožňuje zadať email a heslo, spracováva prihlásenie a naviguje po úspešnom prihlásení.
 *
 * @param navController Navigácia v aplikácii.
 * @param authViewModel ViewModel pre autentifikáciu používateľa.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val error1 = stringResource(R.string.error1)
    val error2 = stringResource(R.string.error2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email), color = colorResource(R.color.dark_gray)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.light_gray),
                unfocusedBorderColor = colorResource(R.color.light_gray),
                cursorColor = colorResource(R.color.light_gray),
                unfocusedLabelColor = colorResource(R.color.light_gray),
                focusedLabelColor = colorResource(R.color.light_gray),
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password), color = colorResource(R.color.dark_gray)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.light_gray),
                unfocusedBorderColor = colorResource(R.color.light_gray),
                cursorColor = colorResource(R.color.light_gray),
                unfocusedLabelColor = colorResource(R.color.light_gray),
                focusedLabelColor = colorResource(R.color.light_gray),
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = error1
                } else {
                    isLoading = true
                    authViewModel.loginUser(email, password) { success, errorMsg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = errorMsg ?: error2
                        } else {
                            navController.navigate("filaments") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.light_gray),
                contentColor = colorResource(R.color.dark_gray)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(stringResource(R.string.login))
            }
        }

        TextButton(
            onClick = { navController.navigate("register") }, // Fixed: Using direct string instead of stringResource
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.to_register),
                color = colorResource(R.color.light_gray)
            )
        }
    }
}