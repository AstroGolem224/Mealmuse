package com.mealmuse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_plan_entries",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("mealPlanId"), Index("recipeId")]
)
data class MealPlanEntryEntity(
    @PrimaryKey val id: String,
    val mealPlanId: String,
    val recipeId: String?,
    val mealType: String,
    val dayOfWeek: Int,
    val scheduledAt: Long?
)
