package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_sessions")
data class AISessionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val prompt: String,
    val response: String,
    val provider: String,
    val createdAt: Long,
    val tokenCount: Int
)
