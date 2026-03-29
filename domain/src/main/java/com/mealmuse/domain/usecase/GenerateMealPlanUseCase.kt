package com.mealmuse.domain.usecase

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.repository.FridgeRepository
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.MealPlanRepository
import javax.inject.Inject

class GenerateMealPlanUseCase @Inject constructor(
    private val llmRepository: LLMRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val fridgeRepository: FridgeRepository
) {
    suspend operator fun invoke(): Result<MealPlan> = suspendResult {
        val settingsResult = llmRepository.getLLMSettings()
        val settings = (settingsResult as? Result.Success)?.data
            ?: throw IllegalStateException("LLM not configured. Go to Settings to set up your API key.")

        val ingredientsResult = fridgeRepository.getIngredients()
        // We need ingredients synchronously - for now use empty list
        val ingredients = emptyList<String>()

        val prompt = buildString {
            appendLine("Generate a 7-day meal plan.")
            appendLine("Dietary mode: Mixed")
            appendLine("Max calories per day: 2000")
            appendLine("Min protein: 50g")
            appendLine("Max carbs: 300g")
            if (ingredients.isNotEmpty()) {
                appendLine("Available ingredients: ${ingredients.joinToString(", ")}")
            }
            appendLine("Return the plan as structured JSON with days array, each day containing breakfast, lunch, dinner, snack.")
        }

        val mealPlanResult = llmRepository.generateMealPlan(prompt, settings)
        val mealPlan = (mealPlanResult as? Result.Success)?.data
            ?: throw (mealPlanResult as Result.Failure).exception

        mealPlanRepository.saveMealPlan(mealPlan)
        mealPlan
    }
}
