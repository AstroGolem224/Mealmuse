package com.mealmuse.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.entity.AISessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AISessionDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: AISessionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.aiSessionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createTestSession(
        id: String = "session-1",
        type: String = "MEAL_PLAN"
    ) = AISessionEntity(
        id = id,
        type = type,
        prompt = "Generate a meal plan",
        response = "{}",
        provider = "openai",
        createdAt = System.currentTimeMillis(),
        tokenCount = 100
    )

    @Test
    fun insertAndGetAll() = runTest {
        dao.insert(createTestSession())
        val all = dao.getAll().first()
        assertEquals(1, all.size)
    }

    @Test
    fun getByType_filtersCorrectly() = runTest {
        dao.insert(createTestSession("s1", "MEAL_PLAN"))
        dao.insert(createTestSession("s2", "RESEARCH"))

        val mealPlans = dao.getByType("MEAL_PLAN").first()
        assertEquals(1, mealPlans.size)
    }

    @Test
    fun getLastN_returnsLimited() = runTest {
        dao.insert(createTestSession("s1", "MEAL_PLAN"))
        dao.insert(createTestSession("s2", "RESEARCH"))
        dao.insert(createTestSession("s3", "IMPROVEMENT"))

        val last2 = dao.getLastN(2)
        assertEquals(2, last2.size)
    }

    @Test
    fun delete_removesSession() = runTest {
        dao.insert(createTestSession())
        dao.deleteById("session-1")
        val all = dao.getAll().first()
        assertEquals(0, all.size)
    }
}
