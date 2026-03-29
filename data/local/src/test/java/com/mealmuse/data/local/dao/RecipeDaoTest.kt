package com.mealmuse.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecipeDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: RecipeDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.recipeDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createTestRecipe(
        id: String = "test-1",
        name: String = "Test Recipe"
    ) = RecipeEntity(
        id = id,
        name = name,
        description = "A test recipe",
        instructions = "[\"Step 1\", \"Step 2\"]",
        prepTimeMinutes = 10,
        cookTimeMinutes = 20,
        servings = 2,
        calories = 500f,
        protein = 30f,
        carbs = 50f,
        fat = 20f,
        sourceUrl = null,
        imageUrl = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isFavorite = false
    )

    @Test
    fun insertAndGetById() = runTest {
        val recipe = createTestRecipe()
        dao.insert(recipe)

        val result = dao.getById("test-1")
        assertNotNull(result)
        assertEquals("Test Recipe", result?.name)
    }

    @Test
    fun getAll_returnsAllRecipes() = runTest {
        dao.insert(createTestRecipe("r1", "Recipe A"))
        dao.insert(createTestRecipe("r2", "Recipe B"))

        val all = dao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun search_findsByName() = runTest {
        dao.insert(createTestRecipe("r1", "Pasta Carbonara"))
        dao.insert(createTestRecipe("r2", "Chicken Curry"))

        val results = dao.search("pasta").first()
        assertEquals(1, results.size)
        assertEquals("Pasta Carbonara", results[0].name)
    }

    @Test
    fun getFavorites_returnsOnlyFavorites() = runTest {
        dao.insert(createTestRecipe("r1", "Fav Recipe"))
        dao.setFavorite("r1", true)
        dao.insert(createTestRecipe("r2", "Not Fav Recipe"))

        val favs = dao.getFavorites().first()
        assertEquals(1, favs.size)
        assertEquals("Fav Recipe", favs[0].name)
    }

    @Test
    fun delete_removesRecipe() = runTest {
        dao.insert(createTestRecipe())
        dao.deleteById("test-1")

        val result = dao.getById("test-1")
        assertNull(result)
    }

    @Test
    fun update_modifiesRecipe() = runTest {
        dao.insert(createTestRecipe())
        val updated = createTestRecipe().copy(name = "Updated Name")
        dao.update(updated)

        val result = dao.getById("test-1")
        assertEquals("Updated Name", result?.name)
    }
}
