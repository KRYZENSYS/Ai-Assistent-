package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE callerName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR transcriptJson LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()
}
