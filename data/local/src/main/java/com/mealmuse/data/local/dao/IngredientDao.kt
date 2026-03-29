package com.mealmuse.data.local.dao

import androidx.room.*
import com.mealmuse.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    fun getAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: String): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE category = :category ORDER BY expiryDate ASC")
    fun getByCategory(category: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE expiryDate IS NOT NULL AND expiryDate < :threshold ORDER BY expiryDate ASC")
    fun getExpiringSoon(threshold: Long): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity)

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: String)
}
