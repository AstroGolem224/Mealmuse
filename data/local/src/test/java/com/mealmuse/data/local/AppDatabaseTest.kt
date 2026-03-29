package com.mealmuse.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun databaseCreatesAllTables() {
        assertTrue(db.recipeDao() != null)
        assertTrue(db.ingredientDao() != null)
        assertTrue(db.mealPlanDao() != null)
        assertTrue(db.tagDao() != null)
        assertTrue(db.preferencesDao() != null)
        assertTrue(db.aiSessionDao() != null)
    }

    @Test
    fun databaseIsOpen() {
        assertTrue(db.isOpen)
    }
}
