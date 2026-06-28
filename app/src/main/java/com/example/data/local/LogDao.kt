package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): kotlinx.coroutines.flow.Flow<List<LogEntity>>

    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("DELETE FROM system_logs")
    suspend fun clearAllLogs()
}
