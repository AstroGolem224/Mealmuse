package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.generateUUID
import com.mealmuse.domain.model.Ingredient
import com.mealmuse.domain.repository.FridgeRepository
import javax.inject.Inject

class ManageFridgeUseCase @Inject constructor(
    private val fridgeRepository: FridgeRepository
) {
    suspend fun addIngredient(name: String, quantity: Float, unit: String, category: com.mealmuse.domain.model.IngredientCategory, expiryDate: Long? = null): Result<Ingredient> {
        val ingredient = Ingredient(
            id = generateUUID(),
            name = name,
            quantity = quantity,
            unit = unit,
            expiryDate = expiryDate,
            category = category,
            addedAt = System.currentTimeMillis()
        )
        return fridgeRepository.addIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: Ingredient): Result<Ingredient> {
        return fridgeRepository.updateIngredient(ingredient)
    }

    suspend fun deleteIngredient(id: String): Result<Unit> {
        return fridgeRepository.deleteIngredient(id)
    }

    fun getAllIngredients() = fridgeRepository.getIngredients()
    fun getExpiringSoon() = fridgeRepository.getExpiringSoon()
    fun searchIngredients(query: String) = fridgeRepository.searchIngredients(query)
}
