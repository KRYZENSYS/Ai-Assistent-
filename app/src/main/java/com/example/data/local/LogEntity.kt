package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val level: String, // "DEBUG", "INFO", "WARN", "ERROR"
    val message: String
)
