package com.example.spoolsync.data.model

import java.util.Date

data class SessionFilament(
    val filamentId: String = "",
    val ownerId: String = "",
    val originalWeight: Int = 0,
    val currentWeight: Int = 0,
    val addedAt: Long = Date().time
)