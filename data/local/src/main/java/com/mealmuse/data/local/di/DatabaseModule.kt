package com.mealmuse.data.local.di

import android.content.Context
import androidx.room.Room
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.dao.*
import com.mealmuse.data.local.repository.RoomFridgeRepository
import com.mealmuse.data.local.repository.RoomMealPlanRepository
import com.mealmuse.data.local.repository.RoomRecipeRepository
import com.mealmuse.data.local.repository.RoomUserPreferencesRepository
import com.mealmuse.domain.repository.FridgeRepository
import com.mealmuse.domain.repository.MealPlanRepository
import com.mealmuse.domain.repository.RecipeRepository
import com.mealmuse.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mealmuse.db"
        ).build()
    }

    @Provides
    fun provideRecipeDao(db: AppDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideIngredientDao(db: AppDatabase): IngredientDao = db.ingredientDao()

    @Provides
    fun provideMealPlanDao(db: AppDatabase): MealPlanDao = db.mealPlanDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    fun providePreferencesDao(db: AppDatabase): PreferencesDao = db.preferencesDao()

    @Provides
    fun provideAISessionDao(db: AppDatabase): AISessionDao = db.aiSessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: RoomMealPlanRepository): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RoomRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindFridgeRepository(impl: RoomFridgeRepository): FridgeRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: RoomUserPreferencesRepository): UserPreferencesRepository
}
