package com.mealmuse.data.local.dao

import androidx.room.*
import com.mealmuse.data.local.entity.MealPlanEntity
import com.mealmuse.data.local.entity.MealPlanEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY weekStart DESC")
    fun getAll(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getById(id: String): MealPlanEntity?

    @Query("SELECT * FROM meal_plans WHERE weekStart >= :start AND weekEnd <= :end")
    fun getByWeekRange(start: Long, end: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plan_entries WHERE mealPlanId = :mealPlanId")
    suspend fun getEntriesByPlanId(mealPlanId: String): List<MealPlanEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: MealPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MealPlanEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<MealPlanEntryEntity>)

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM meal_plan_entries WHERE mealPlanId = :mealPlanId")
    suspend fun deleteEntriesByPlanId(mealPlanId: String)
}
