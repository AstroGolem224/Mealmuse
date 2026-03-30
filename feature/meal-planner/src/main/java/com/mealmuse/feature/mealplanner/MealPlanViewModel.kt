package com.mealmuse.feature.mealplanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.MealPlan
import com.mealmuse.domain.model.MealPlanEntry
import com.mealmuse.domain.model.MealType
import com.mealmuse.domain.usecase.GenerateMealPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealPlanUiState(
    val currentPlan: MealPlan? = null,
    val isGenerating: Boolean = false,
    val generationProgress: String? = null,
    val error: String? = null,
    val weekOffset: Int = 0,
    val selectedDuration: Int = 7
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val generateMealPlanUseCase: GenerateMealPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    fun setDuration(days: Int) {
        _uiState.value = _uiState.value.copy(selectedDuration = days, weekOffset = 0)
    }

    fun generatePlan() {
        val duration = _uiState.value.selectedDuration
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                error = null,
                generationProgress = null
            )
            try {
                when (val result = generateMealPlanUseCase(
                    durationDays = duration,
                    onChunkComplete = { done, total ->
                        _uiState.value = _uiState.value.copy(
                            generationProgress = "Part $done of $total..."
                        )
                    }
                )) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            currentPlan = result.data,
                            isGenerating = false,
                            generationProgress = null
                        )
                    }
                    is Result.Failure -> {
                        val errorMsg = result.exception.message ?: "Failed to generate meal plan"
                        Log.e("MealPlanVM", "Generate failed: $errorMsg", result.exception)
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            generationProgress = null,
                            error = errorMsg
                        )
                    }
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                Log.e("MealPlanVM", "Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generationProgress = null,
                    error = e.message ?: "Unknown error: ${e::class.simpleName}"
                )
            }
        }
    }

    fun nextWeek() {
        val maxOffset = if (_uiState.value.selectedDuration >= 14) 1 else 0
        _uiState.value = _uiState.value.copy(
            weekOffset = (_uiState.value.weekOffset + 1).coerceAtMost(maxOffset)
        )
    }

    fun previousWeek() {
        _uiState.value = _uiState.value.copy(
            weekOffset = (_uiState.value.weekOffset - 1).coerceAtLeast(0)
        )
    }

    fun getEntriesForDay(dayOfWeek: Int): List<MealPlanEntry> =
        _uiState.value.currentPlan?.entries?.filter { it.dayOfWeek == dayOfWeek } ?: emptyList()

    fun getEntry(dayOfWeek: Int, mealType: MealType): MealPlanEntry? =
        _uiState.value.currentPlan?.entries?.find {
            it.dayOfWeek == dayOfWeek && it.mealType == mealType
        }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
