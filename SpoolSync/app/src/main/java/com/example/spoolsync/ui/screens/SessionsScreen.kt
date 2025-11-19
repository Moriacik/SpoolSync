package com.example.spoolsync.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.data.model.Session
import com.example.spoolsync.ui.components.SessionDialog
import com.example.spoolsync.ui.components.SessionDialogMode
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.SessionsViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    navController: NavController,
    sessionsViewModel: SessionsViewModel
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    val sessions by sessionsViewModel.sessions.collectAsState()

    LaunchedEffect(Unit) {
        sessionsViewModel.loadUserSessions()
    }

    if (showCreateDialog) {
        SessionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                sessionsViewModel.createSession(it)
                showCreateDialog = false
            },
            dialogMode = SessionDialogMode.CREATE,
            isLoading = sessionsViewModel.isLoading.collectAsState().value,
            errorMessage = sessionsViewModel.errorMessage.collectAsState().value
        )
    }

    if (showJoinDialog) {
        SessionDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = {
                sessionsViewModel.joinSession(it)
                showJoinDialog = false
            },
            dialogMode = SessionDialogMode.JOIN,
            isLoading = sessionsViewModel.isLoading.collectAsState().value,
            errorMessage = sessionsViewModel.errorMessage.collectAsState().value
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sessions),
                            contentDescription = stringResource(R.string.sessions),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.sessions), fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SpoolSyncTheme.colors.lighterGrayDarkerGray
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
                                tint = colorResource(R.color.gray),
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
                                tint = colorResource(R.color.gray),
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
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_sessions),
                                contentDescription = stringResource(R.string.sessions),
                                tint = SpoolSyncTheme.colors.blackWhite,
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
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (sessions.isNotEmpty()) {
                sessions.forEach { session ->
                    SessionItem(session, navController)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray)
            ) {
                Text(stringResource(R.string.create_session), color = colorResource(R.color.white))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showJoinDialog = true },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray)
            ) {
                Text(stringResource(R.string.join_session), color = colorResource(R.color.white))
            }
        }
    }
}

@Composable
fun SessionItem(
    session: Session,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navController.navigate("sessionDetail/${session.id}") },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.name,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${session.accessCode}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = if (session.ownerId == FirebaseAuth.getInstance().currentUser?.uid) "[owner]" else "[member]",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        HorizontalDivider(color = SpoolSyncTheme.colors.lightGrayDarkGray)
    }
}