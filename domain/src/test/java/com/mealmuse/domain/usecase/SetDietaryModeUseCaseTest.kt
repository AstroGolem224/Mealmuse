package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import org.junit.Assert.*
import org.junit.Test

class SetDietaryModeUseCaseTest {

    private val useCase = SetDietaryModeUseCase()

    @Test
    fun `set Keto mode updates preferences`() {
        val prefs = UserPreferences(dietaryModes = emptyList())
        val result = useCase(prefs, DietaryMode.Keto)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertTrue(updated.dietaryModes.contains(DietaryMode.Keto))
    }

    @Test
    fun `set Keto mode sets appropriate macros`() {
        val prefs = UserPreferences(
            dietaryModes = emptyList(),
            maxCalories = 2000,
            minProtein = 50,
            maxCarbs = 300,
            maxFat = 70
        )
        val result = useCase(prefs, DietaryMode.Keto)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertEquals(50, updated.maxCarbs) // Keto: low carbs
        assertEquals(150, updated.maxFat) // Keto: high fat
        assertEquals(150, updated.minProtein) // Keto: high protein
    }

    @Test
    fun `set Vegan mode sets appropriate macros`() {
        val prefs = UserPreferences(dietaryModes = emptyList())
        val result = useCase(prefs, DietaryMode.Vegan)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertTrue(updated.dietaryModes.contains(DietaryMode.Vegan))
        assertEquals(300, updated.maxCarbs) // Vegan: higher carbs
    }

    @Test
    fun `set CalorieDeficit mode reduces calories`() {
        val prefs = UserPreferences(
            dietaryModes = emptyList(),
            maxCalories = 2000
        )
        val result = useCase(prefs, DietaryMode.CalorieDeficit)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertEquals(1500, updated.maxCalories) // 500 cal deficit
    }

    @Test
    fun `toggling existing mode removes it`() {
        val prefs = UserPreferences(dietaryModes = listOf(DietaryMode.Keto))
        val result = useCase(prefs, DietaryMode.Keto)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertFalse(updated.dietaryModes.contains(DietaryMode.Keto))
    }

    @Test
    fun `adding multiple modes works`() {
        val prefs = UserPreferences(dietaryModes = listOf(DietaryMode.Keto))
        val result = useCase(prefs, DietaryMode.Vegan)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertTrue(updated.dietaryModes.contains(DietaryMode.Keto))
        assertTrue(updated.dietaryModes.contains(DietaryMode.Vegan))
    }

    @Test
    fun `set custom mode works`() {
        val prefs = UserPreferences(dietaryModes = emptyList())
        val customMode = DietaryMode.Custom("My Custom", maxCarbs = 100)
        val result = useCase(prefs, customMode)

        assertTrue(result is Result.Success)
        val updated = (result as Result.Success).data
        assertTrue(updated.dietaryModes.any { it is DietaryMode.Custom })
    }
}
