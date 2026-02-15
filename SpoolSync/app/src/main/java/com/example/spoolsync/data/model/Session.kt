package com.example.spoolsync.data.model

import java.io.Serializable

data class Session(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val accessCode: String = "",
    val participants: List<String> = emptyList(),
    val filaments: List<Map<String, Any>> = emptyList(),
    val sessionCreatedAt: Long = 0,
    val lastModifiedAt: Long = 0
) : Serializable