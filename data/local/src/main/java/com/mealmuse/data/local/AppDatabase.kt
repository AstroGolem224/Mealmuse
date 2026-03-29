package com.mealmuse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mealmuse.data.local.dao.RecipeDao
import com.mealmuse.data.local.dao.IngredientDao
import com.mealmuse.data.local.dao.MealPlanDao
import com.mealmuse.data.local.dao.TagDao
import com.mealmuse.data.local.dao.PreferencesDao
import com.mealmuse.data.local.dao.AISessionDao
import com.mealmuse.data.local.entity.RecipeEntity
import com.mealmuse.data.local.entity.RecipeIngredientEntity
import com.mealmuse.data.local.entity.IngredientEntity
import com.mealmuse.data.local.entity.MealPlanEntity
import com.mealmuse.data.local.entity.MealPlanEntryEntity
import com.mealmuse.data.local.entity.TagEntity
import com.mealmuse.data.local.entity.RecipeTagEntity
import com.mealmuse.data.local.entity.UserPreferencesEntity
import com.mealmuse.data.local.entity.AISessionEntity

@Database(
    entities = [
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        IngredientEntity::class,
        MealPlanEntity::class,
        MealPlanEntryEntity::class,
        TagEntity::class,
        RecipeTagEntity::class,
        UserPreferencesEntity::class,
        AISessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun tagDao(): TagDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun aiSessionDao(): AISessionDao
}
