package com.mealmuse.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mealmuse.data.local.AppDatabase
import com.mealmuse.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PreferencesDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PreferencesDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.preferencesDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetPreferences() = runTest {
        val prefs = UserPreferencesEntity(
            dietaryModes = "[\"KETO\"]",
            maxCalories = 2000,
            minProtein = 50,
            maxCarbs = 50,
            maxFat = 70
        )
        dao.insertOrUpdate(prefs)

        val result = dao.getOnce()
        assertNotNull(result)
        assertEquals(2000, result?.maxCalories)
    }

    @Test
    fun updatePreferences() = runTest {
        val prefs = UserPreferencesEntity(
            dietaryModes = "[\"KETO\"]",
            maxCalories = 2000,
            minProtein = 50,
            maxCarbs = 50,
            maxFat = 70
        )
        dao.insertOrUpdate(prefs)

        val updated = prefs.copy(maxCalories = 1800)
        dao.insertOrUpdate(updated)

        val result = dao.getOnce()
        assertEquals(1800, result?.maxCalories)
    }
}
