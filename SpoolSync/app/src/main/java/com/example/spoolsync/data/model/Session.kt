package com.example.spoolsync.data.model

import java.util.Date

data class Session(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val accessCode: String = "",
    val participants: List<String> = emptyList(),
    val filaments: List<String> = emptyList(),
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time
)