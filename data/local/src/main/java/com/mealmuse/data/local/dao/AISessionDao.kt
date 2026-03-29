package com.mealmuse.data.local.dao

import androidx.room.*
import com.mealmuse.data.local.entity.AISessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AISessionDao {
    @Query("SELECT * FROM ai_sessions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<AISessionEntity>>

    @Query("SELECT * FROM ai_sessions WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<AISessionEntity>>

    @Query("SELECT * FROM ai_sessions ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getLastN(limit: Int): List<AISessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AISessionEntity)

    @Query("DELETE FROM ai_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
