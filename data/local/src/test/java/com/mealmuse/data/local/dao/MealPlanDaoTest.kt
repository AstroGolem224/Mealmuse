package com.mealmuse.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.entity.MealPlanEntity
import com.mealmuse.data.local.entity.MealPlanEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MealPlanDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var mealPlanDao: MealPlanDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        mealPlanDao = db.mealPlanDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createTestPlan(
        id: String = "plan-1",
        name: String = "Week 1",
        weekStart: Long = System.currentTimeMillis()
    ) = MealPlanEntity(
        id = id,
        name = name,
        weekStart = weekStart,
        weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000),
        dietaryMode = "KETO",
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun insertAndGetById() = runTest {
        mealPlanDao.insert(createTestPlan())
        val result = mealPlanDao.getById("plan-1")
        assertNotNull(result)
        assertEquals("Week 1", result?.name)
    }

    @Test
    fun getAll_returnsAllPlans() = runTest {
        mealPlanDao.insert(createTestPlan("p1", "Plan A"))
        mealPlanDao.insert(createTestPlan("p2", "Plan B"))

        val all = mealPlanDao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun getEntriesByPlanId_returnsEntries() = runTest {
        mealPlanDao.insert(createTestPlan())
        mealPlanDao.insertEntry(MealPlanEntryEntity(
            id = "entry-1",
            mealPlanId = "plan-1",
            recipeId = null,
            mealType = "BREAKFAST",
            dayOfWeek = 1,
            scheduledAt = null
        ))

        val entries = mealPlanDao.getEntriesByPlanId("plan-1")
        assertEquals(1, entries.size)
        assertEquals("BREAKFAST", entries[0].mealType)
    }

    @Test
    fun delete_removesPlan() = runTest {
        mealPlanDao.insert(createTestPlan())
        mealPlanDao.deleteById("plan-1")
        assertNull(mealPlanDao.getById("plan-1"))
    }

    @Test
    fun delete_removesAssociatedEntries() = runTest {
        mealPlanDao.insert(createTestPlan())
        mealPlanDao.insertEntry(MealPlanEntryEntity(
            id = "entry-1",
            mealPlanId = "plan-1",
            recipeId = null,
            mealType = "LUNCH",
            dayOfWeek = 2,
            scheduledAt = null
        ))

        mealPlanDao.deleteEntriesByPlanId("plan-1")
        val entries = mealPlanDao.getEntriesByPlanId("plan-1")
        assertEquals(0, entries.size)
    }
}
