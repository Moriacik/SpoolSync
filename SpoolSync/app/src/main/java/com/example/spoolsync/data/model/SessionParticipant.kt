package com.example.spoolsync.data.model

import java.util.Date

data class SessionParticipant (
    val userId: String = "",
    val joinedAt: Long = Date().time,
    val role: String = "participant"
)