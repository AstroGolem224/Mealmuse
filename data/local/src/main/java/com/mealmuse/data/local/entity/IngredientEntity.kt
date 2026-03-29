package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val expiryDate: Long?,
    val category: String,
    val addedAt: Long
)
