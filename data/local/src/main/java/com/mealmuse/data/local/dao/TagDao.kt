package com.mealmuse.data.local.dao

import androidx.room.*
import com.mealmuse.data.local.entity.TagEntity
import com.mealmuse.data.local.entity.RecipeTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT t.* FROM tags t INNER JOIN recipe_tags rt ON t.id = rt.tagId WHERE rt.recipeId = :recipeId")
    fun getTagsByRecipeId(recipeId: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTagToRecipe(recipeTag: RecipeTagEntity)

    @Query("DELETE FROM recipe_tags WHERE recipeId = :recipeId AND tagId = :tagId")
    suspend fun removeTagFromRecipe(recipeId: String, tagId: String)
}
