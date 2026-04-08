package com.mealmuse.domain.usecase

import android.util.Log
import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.MealPlanEntry
import com.mealmuse.domain.repository.FridgeRepository
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.domain.repository.MealPlanRepository
import com.mealmuse.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GenerateMealPlanUseCase @Inject constructor(
    private val llmRepository: LLMRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val fridgeRepository: FridgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "GenerateMealPlanUseCase"
        private const val CHUNK_SIZE = 3

        private val VARIATION_STYLES = listOf(
            "Mediterranean-inspired cuisine — olive oil, legumes, feta, fresh herbs",
            "Asian fusion — stir-fries, rice bowls, miso, ginger-forward flavors",
            "Classic comfort food reimagined with lighter, nutritious twists",
            "Plant-forward meals with bold spices, roasted vegetables, and whole grains",
            "Quick 30-minute weeknight cooking, minimal prep, maximum flavor",
            "Latin-inspired dishes — beans, corn, avocado, bright citrus",
            "Middle Eastern flavors — tahini, za'atar, chickpeas, flatbreads",
            "Japanese-inspired simplicity — clean broths, grilled proteins, pickled sides"
        )
    }

    suspend operator fun invoke(
        durationDays: Int = 7,
        onChunkComplete: (completedChunks: Int, totalChunks: Int) -> Unit = { _, _ -> }
    ): Result<MealPlan> = suspendResult {
        Log.d(TAG, "Starting meal plan generation — durationDays=$durationDays")

        val settings = (llmRepository.getLLMSettings() as? Result.Success)?.data
            ?: throw IllegalStateException("Please configure AI in Settings first.")
        if (settings.apiKey.isBlank()) throw IllegalStateException("Please enter your API key in Settings first.")
        if (settings.model.isBlank()) throw IllegalStateException("Please select a model in Settings first.")

        Log.d(TAG, "Settings: provider=${settings.provider}, model=${settings.model}")

        val preferences = userPreferencesRepository.getPreferencesOnce()
        val fridgeIngredients = (fridgeRepository.getIngredients().firstOrNull() as? Result.Success)
            ?.data?.map { it.name } ?: emptyList()

        val dietaryModeStr = when (val mode = preferences.dietaryModes.firstOrNull()) {
            is DietaryMode.Keto -> "KETO"
            is DietaryMode.LowCarb -> "LOW_CARB"
            is DietaryMode.Vegetarian -> "VEGETARIAN"
            is DietaryMode.Vegan -> "VEGAN"
            is DietaryMode.Paleo -> "PALEO"
            is DietaryMode.CalorieDeficit -> "CALORIE_DEFICIT"
            is DietaryMode.Custom -> mode.name
            null -> "BALANCED"
        }
        val actualDietaryMode = preferences.dietaryModes.firstOrNull() ?: DietaryMode.Keto

        val chunks = buildChunks(durationDays)
        val variationSeed = (0..999).random()
        Log.d(TAG, "Generating $durationDays days in ${chunks.size} chunk(s), seed=$variationSeed")

        val allEntries = mutableListOf<MealPlanEntry>()

        chunks.forEachIndexed { index, (startDay, endDay) ->
            Log.d(TAG, "Chunk ${index + 1}/${chunks.size}: days $startDay–$endDay")
            val prompt = buildPrompt(
                ingredients = fridgeIngredients,
                dietaryMode = dietaryModeStr,
                maxCalories = preferences.maxCalories,
                minProtein = preferences.minProtein,
                maxCarbs = preferences.maxCarbs,
                startDay = startDay,
                endDay = endDay,
                variationHint = variationSeed + index
            )
        val chunkResult = llmRepository.generateMealPlan(prompt, settings)
        val chunkPlan = when (chunkResult) {
            is Result.Success -> chunkResult.data
            is Result.Failure -> throw chunkResult.exception
            is Result.Loading -> throw IllegalStateException("Unexpected loading state during meal plan generation")
        }

            allEntries.addAll(chunkPlan.entries)
            onChunkComplete(index + 1, chunks.size)
            Log.d(TAG, "Chunk ${index + 1} done — ${chunkPlan.entries.size} entries")
        }

        val now = System.currentTimeMillis()
        val mergedPlan = MealPlan(
            id = "generated-$now",
            name = when (durationDays) {
                1 -> "1-Day Meal Plan"
                3 -> "3-Day Meal Plan"
                7 -> "Weekly Meal Plan"
                14 -> "2-Week Meal Plan"
                else -> "$durationDays-Day Meal Plan"
            },
            weekStart = now,
            weekEnd = now + (durationDays.toLong() * 24 * 60 * 60 * 1000),
            entries = allEntries,
            dietaryMode = actualDietaryMode,
            createdAt = now
        )

        mealPlanRepository.saveMealPlan(mergedPlan)
        Log.d(TAG, "Meal plan saved — ${allEntries.size} total entries")
        mergedPlan
    }

    private fun buildChunks(durationDays: Int): List<Pair<Int, Int>> {
        val chunks = mutableListOf<Pair<Int, Int>>()
        var day = 1
        while (day <= durationDays) {
            val end = minOf(day + CHUNK_SIZE - 1, durationDays)
            chunks.add(day to end)
            day = end + 1
        }
        return chunks
    }

    private fun buildPrompt(
        ingredients: List<String>,
        dietaryMode: String,
        maxCalories: Int,
        minProtein: Int,
        maxCarbs: Int,
        startDay: Int,
        endDay: Int,
        variationHint: Int
    ): String = buildString {
        val dayCount = endDay - startDay + 1
        val style = VARIATION_STYLES[variationHint % VARIATION_STYLES.size]

        appendLine("You are a professional nutritionist and chef.")
        appendLine("Generate a meal plan for days $startDay through $endDay ($dayCount day${if (dayCount > 1) "s" else ""}).")
        appendLine()
        appendLine("Requirements:")
        appendLine("- Dietary mode: $dietaryMode")
        appendLine("- Max calories per day: $maxCalories kcal")
        appendLine("- Min protein per day: ${minProtein}g")
        appendLine("- Max carbs per day: ${maxCarbs}g")
        appendLine("- 4 meals per day: BREAKFAST, LUNCH, DINNER, SNACK")
        appendLine("- dayOfWeek must use the exact integers $startDay through $endDay")
        appendLine()
        appendLine("Style: $style")
        appendLine("Variation rules:")
        appendLine("- Do NOT repeat the same protein source on consecutive days")
        appendLine("- Use at least 3 different cooking methods (grilled, baked, steamed, stir-fried, raw, etc.)")
        appendLine("- Vary the grain/carb base each day (rice, quinoa, oats, bread, pasta, potatoes, etc.)")
        appendLine("- Give each recipe a unique, descriptive name — no generic names like 'Chicken Salad'")
        appendLine()
        if (ingredients.isNotEmpty()) {
            appendLine("Available ingredients to incorporate: ${ingredients.joinToString(", ")}")
            appendLine()
        }
        val totalEntries = dayCount * 4
        appendLine("CRITICAL: You MUST generate EXACTLY $totalEntries entries total ($dayCount days × 4 meals each).")
        appendLine("Every day from $startDay to $endDay must have all 4 meal types: BREAKFAST, LUNCH, DINNER, SNACK.")
        appendLine()
        appendLine("Return ONLY valid JSON — no markdown, no explanation — using this exact structure:")
        val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
        val exampleEntries = (startDay..endDay).flatMap { day ->
            mealTypes.map { meal -> """    {"id":"entry-$day-$meal","dayOfWeek":$day,"mealType":"$meal","recipe":{"id":"r-$day-${meal.take(1).lowercase()}","name":"REPLACE_WITH_REAL_NAME","description":"REPLACE","instructions":["step1"],"prepTimeMinutes":10,"cookTimeMinutes":20,"servings":1,"calories":400,"protein":30,"carbs":40,"fat":15}}""" }
        }.joinToString(",\n")
        append("""{
  "id": "chunk-$startDay",
  "name": "Meal Plan",
  "weekStart": 0,
  "weekEnd": 0,
  "entries": [
$exampleEntries
  ],
  "dietaryMode": "$dietaryMode"
}""")
    }
}
