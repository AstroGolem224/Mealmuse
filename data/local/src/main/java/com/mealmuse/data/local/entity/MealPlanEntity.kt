package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val weekStart: Long,
    val weekEnd: Long,
    val dietaryMode: String,
    val createdAt: Long
)
