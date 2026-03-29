package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val dietaryModes: String, // JSON array
    val maxCalories: Int,
    val minProtein: Int,
    val maxCarbs: Int,
    val maxFat: Int
)
