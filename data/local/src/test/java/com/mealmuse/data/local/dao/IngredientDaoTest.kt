package com.mealmuse.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IngredientDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: IngredientDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.ingredientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createTestIngredient(
        id: String = "ing-1",
        name: String = "Test Ingredient",
        category: String = "PRODUCE",
        expiryDate: Long? = null
    ) = IngredientEntity(
        id = id,
        name = name,
        quantity = 2.0f,
        unit = "kg",
        expiryDate = expiryDate,
        category = category,
        addedAt = System.currentTimeMillis()
    )

    @Test
    fun insertAndGetById() = runTest {
        dao.insert(createTestIngredient())
        val result = dao.getById("ing-1")
        assertNotNull(result)
        assertEquals("Test Ingredient", result?.name)
    }

    @Test
    fun getByCategory_filtersCorrectly() = runTest {
        dao.insert(createTestIngredient("i1", "Apple", "PRODUCE"))
        dao.insert(createTestIngredient("i2", "Milk", "DAIRY"))

        val produce = dao.getByCategory("PRODUCE").first()
        assertEquals(1, produce.size)
        assertEquals("Apple", produce[0].name)
    }

    @Test
    fun getExpiringSoon_findsNearExpiry() = runTest {
        val twoDaysFromNow = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000)
        val tenDaysFromNow = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000)

        dao.insert(createTestIngredient("i1", "Expiring Soon", "PRODUCE", twoDaysFromNow))
        dao.insert(createTestIngredient("i2", "Fresh", "PRODUCE", tenDaysFromNow))

        val threshold = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)
        val expiring = dao.getExpiringSoon(threshold).first()
        assertEquals(1, expiring.size)
        assertEquals("Expiring Soon", expiring[0].name)
    }

    @Test
    fun search_findsByName() = runTest {
        dao.insert(createTestIngredient("i1", "Tomato"))
        dao.insert(createTestIngredient("i2", "Potato"))

        val results = dao.search("tom").first()
        assertEquals(1, results.size)
    }

    @Test
    fun delete_removesIngredient() = runTest {
        dao.insert(createTestIngredient())
        dao.deleteById("ing-1")
        assertNull(dao.getById("ing-1"))
    }
}
