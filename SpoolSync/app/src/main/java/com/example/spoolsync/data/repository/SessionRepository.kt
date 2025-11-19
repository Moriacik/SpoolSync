package com.example.spoolsync.data.repository

import com.example.spoolsync.data.model.Session
import com.example.spoolsync.data.model.SessionFilament
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class SessionRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun createSession(sessionName: String): Result<Session> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val accessCode = generateAccessCode()
            val sessionId = firestore.collection("sessions").document().id

            val session = Session(
                id = sessionId,
                name = sessionName,
                ownerId = userId,
                accessCode = accessCode,
                participants = listOf(userId),
                createdAt = Date().time,
                updatedAt = Date().time
            )

            firestore.collection("sessions").document(sessionId).set(session).await()
            firestore.collection("users").document(userId).collection("sessions").document(sessionId).set(mapOf("joinedAt" to Date().time)).await()

            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinSession(accessCode: String): Result<Session> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val querySnapshot = firestore.collection("sessions")
                .whereEqualTo("accessCode", accessCode)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Session not found"))
            }

            val sessionDocument = querySnapshot.documents.first()
            val session = sessionDocument.toObject(Session::class.java) ?: return Result.failure(Exception("Failed to parse session"))

            if (session.participants.contains(userId)) {
                return Result.failure(Exception("User already in session"))
            }

            val updatedParticipants = session.participants + userId
            firestore.collection("sessions").document(session.id).update("participants", updatedParticipants).await()
            firestore.collection("users").document(userId).collection("sessions").document(session.id).set(mapOf("joinedAt" to Date().time)).await()

            Result.success(session.copy(participants = updatedParticipants))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserSessions(): Result<List<Session>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .get()
                .await()

            val sessions = mutableListOf<Session>()
            for (document in querySnapshot.documents) {
                val sessionId = document.id
                val sessionSnapshot = firestore.collection("sessions").document(sessionId).get().await()
                val session = sessionSnapshot.toObject(Session::class.java)
                if (session != null) {
                    sessions.add(session)
                }
            }

            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSession(sessionId: String): Result<Session> {
        return try {
            val sessionSnapshot = firestore.collection("sessions").document(sessionId).get().await()
            val session = sessionSnapshot.toObject(Session::class.java) ?: return Result.failure(Exception("Session not found"))
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFilamentToSession(sessionId: String, filamentId: String, ownerId: String, originalWeight: Int): Result<Unit> {
        return try {
            val sessionFilament = SessionFilament(
                filamentId = filamentId,
                ownerId = ownerId,
                originalWeight = originalWeight,
                currentWeight = originalWeight,
                addedAt = Date().time
            )

            firestore.collection("sessions")
                .document(sessionId)
                .collection("filaments")
                .document(filamentId)
                .set(sessionFilament)
                .await()

            firestore.collection("sessions")
                .document(sessionId)
                .update("filaments", com.google.firebase.firestore.FieldValue.arrayUnion(filamentId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFilamentFromSession(sessionId: String, filamentId: String): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(sessionId)
                .collection("filaments")
                .document(filamentId)
                .delete()
                .await()

            firestore.collection("sessions")
                .document(sessionId)
                .update("filaments", com.google.firebase.firestore.FieldValue.arrayRemove(filamentId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveSession(sessionId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val sessionSnapshot = firestore.collection("sessions").document(sessionId).get().await()
            val session = sessionSnapshot.toObject(Session::class.java) ?: return Result.failure(Exception("Session not found"))

            val updatedParticipants = session.participants.filter { it != userId }

            if (updatedParticipants.isEmpty()) {
                firestore.collection("sessions").document(sessionId).delete().await()
            } else {
                val newOwner = if (session.ownerId == userId) updatedParticipants.first() else session.ownerId

                firestore.collection("sessions").document(sessionId).update(
                    mapOf(
                        "participants" to updatedParticipants,
                        "ownerId" to newOwner
                    )
                ).await()

                val filamentSnapshot = firestore.collection("sessions")
                    .document(sessionId)
                    .collection("filaments")
                    .get()
                    .await()

                for (document in filamentSnapshot.documents) {
                    val sessionFilament = document.toObject(SessionFilament::class.java)
                    if (sessionFilament?.ownerId == userId) {
                        removeFilamentFromSession(sessionId, document.id)
                    }
                }
            }

            firestore.collection("users").document(userId).collection("sessions").document(sessionId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFilamentWeightInSession(sessionId: String, filamentId: String, newWeight: Int): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(sessionId)
                .collection("filaments")
                .document(filamentId)
                .update("currentWeight", newWeight)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateAccessCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}