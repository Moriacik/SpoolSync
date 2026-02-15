package com.example.spoolsync.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.BottomNavigationBar
import com.example.spoolsync.ui.components.FilamentList
import com.example.spoolsync.ui.components.NavigationItem
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.SessionsViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    navController: NavController,
    sessionId: String,
    sessionsViewModel: SessionsViewModel = viewModel()
) {
    val sessionFilaments by sessionsViewModel.sessionFilaments.collectAsState()
    val currentSession by sessionsViewModel.currentSession.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isOwner = currentSession?.ownerId == currentUserId

    LaunchedEffect(sessionId) {
        sessionsViewModel.loadSession(sessionId)
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
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {
                            // TODO: Navigovať na settings screen pre session
                            navController.navigate("sessionSettings/${sessionId}")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = SpoolSyncTheme.colors.blackWhite
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, selectedItem = NavigationItem.SESSIONS)
        }
    ) { innerPadding ->
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val isOwner = currentSession?.ownerId == currentUserId

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            FilamentList(
                filaments = sessionFilaments,
                navController = navController,
                modifier = Modifier.weight(1f),
                onFilamentClick = { filament ->
                    // navigate to a session-aware filament view/edit
                    navController.navigate("sessionFilamentView/${sessionId}/${filament.id}")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Leave session button - only show if user is NOT owner
            if (!isOwner) {
                Button(
                    onClick = {
                        sessionsViewModel.leaveSession(sessionId)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpoolSyncTheme.colors.lightGrayGray
                    )
                ) {
                    Text(
                        text = stringResource(R.string.leave_session),
                        color = colorResource(R.color.white),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}