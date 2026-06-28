package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String,
    val transcriptJson: String, // Stored as serialized JSON list of conversation turns
    val isFavorite: Boolean = false,
    val durationSeconds: Int = 0
)
