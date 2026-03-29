package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val instructions: String, // JSON array of steps
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sourceUrl: String?,
    val imageUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean
)
