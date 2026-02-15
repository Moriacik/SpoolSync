package com.example.spoolsync.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.SessionsViewModel
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "SessionSettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSettingsScreen(
    navController: NavController,
    sessionId: String,
    sessionsViewModel: SessionsViewModel = viewModel()
) {
    Log.d(TAG, "Screen opened for sessionId: $sessionId")

    val currentSession by sessionsViewModel.currentSession.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Log.d(TAG, "currentSession: ${currentSession?.name}, currentUserId: $currentUserId")

    var sessionName by remember(currentSession?.name) {
        mutableStateOf(currentSession?.name ?: "")
    }
    var isEditingName by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        Log.d(TAG, "Loading session: $sessionId")
        sessionsViewModel.loadSession(sessionId)
    }

    Log.d(TAG, "Building UI")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Session Name Section
                    Text(
                        text = stringResource(R.string.session_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        if (isEditingName) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .background(SpoolSyncTheme.colors.lightGrayGray, RoundedCornerShape(10.dp))
                            ) {
                                TextField(
                                    value = sessionName,
                                    onValueChange = { sessionName = it },
                                    placeholder = { Text(stringResource(R.string.session_name), color = colorResource(R.color.gray)) },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = SpoolSyncTheme.colors.blackWhite),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(12.dp)
                                )
                                Button(
                                    onClick = {
                                        if (sessionName.isNotEmpty() && currentSession != null) {
                                            Log.d(TAG, "Saving session name: $sessionName")
                                            sessionsViewModel.updateSessionName(sessionId, sessionName)
                                            isEditingName = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SpoolSyncTheme.colors.lightGrayGray
                                    ),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.save),
                                        color = colorResource(R.color.white),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentSession?.name ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SpoolSyncTheme.colors.blackWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { isEditingName = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SpoolSyncTheme.colors.lightGrayGray
                                    ),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.edit),
                                        color = colorResource(R.color.white),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Participants Section
                    Text(
                        text = stringResource(R.string.participants),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            Log.d(TAG, "Participants count: ${currentSession?.participants?.size ?: 0}")

            val participantsList = currentSession?.participants ?: emptyList()
            val ownerId = currentSession?.ownerId ?: ""

            if (participantsList.isNotEmpty()) {
                Log.d(TAG, "Adding ${participantsList.size} participant items")
                items(participantsList) { participantId ->
                    Log.d(TAG, "Rendering participant: $participantId")

                    Box(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    ) {
                        ParticipantItem(
                            participantId = participantId,
                            isOwner = ownerId == participantId,
                            currentUserId = currentUserId,
                            onRemove = {
                                Log.d(TAG, "Removing participant: $participantId")
                                if (ownerId == currentUserId) {
                                    sessionsViewModel.removeParticipantFromSession(sessionId, participantId)
                                }
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            } else {
                item {
                    Log.d(TAG, "No participants to display")
                    Text(
                        text = stringResource(R.string.no_participants),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.gray),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

            // Leave Session Button - aligned to bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        Log.d(TAG, "End session: $sessionId")
                        sessionsViewModel.leaveSession(sessionId)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.red)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.leave_session),
                        color = colorResource(R.color.white),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantItem(
    participantId: String,
    isOwner: Boolean,
    currentUserId: String,
    onRemove: () -> Unit
) {
    Log.d(TAG, "ParticipantItem START: participantId=$participantId, isOwner=$isOwner")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Log.d(TAG, "ParticipantItem: Building avatar")
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SpoolSyncTheme.colors.lightGrayGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participantId.take(2).uppercase(),
                    color = colorResource(R.color.white),
                    fontWeight = FontWeight.Bold
                )
            }

            Log.d(TAG, "ParticipantItem: Building info column")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = if (isOwner) stringResource(R.string.owner) else stringResource(R.string.member),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpoolSyncTheme.colors.blackWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = participantId,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpoolSyncTheme.colors.darkGrayGray
                )
            }
        }

        Log.d(TAG, "ParticipantItem: Checking remove button - participantId=$participantId, currentUserId=$currentUserId")
        // Remove button (only for owner, and not for themselves)
        if (participantId != currentUserId) {
            Log.d(TAG, "ParticipantItem: Showing remove button")
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    tint = colorResource(R.color.red),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Log.d(TAG, "ParticipantItem: NOT showing remove button (same user)")
        }
    }
    Log.d(TAG, "ParticipantItem END: Completed successfully")
}


