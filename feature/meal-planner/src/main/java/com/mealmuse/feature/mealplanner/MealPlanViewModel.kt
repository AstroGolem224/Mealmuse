package com.mealmuse.feature.mealplanner

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
    val error: String? = null,
    val weekOffset: Int = 0
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val generateMealPlanUseCase: GenerateMealPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    fun generatePlan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
            when (val result = generateMealPlanUseCase()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentPlan = result.data,
                        isGenerating = false
                    )
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = result.exception.message ?: "Failed to generate meal plan"
                    )
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    fun nextWeek() {
        _uiState.value = _uiState.value.copy(weekOffset = _uiState.value.weekOffset + 1)
    }

    fun previousWeek() {
        _uiState.value = _uiState.value.copy(weekOffset = (_uiState.value.weekOffset - 1).coerceAtLeast(0))
    }

    fun getEntriesForDay(dayOfWeek: Int): List<MealPlanEntry> {
        return _uiState.value.currentPlan?.entries?.filter { it.dayOfWeek == dayOfWeek } ?: emptyList()
    }

    fun getEntry(dayOfWeek: Int, mealType: MealType): MealPlanEntry? {
        return _uiState.value.currentPlan?.entries?.find {
            it.dayOfWeek == dayOfWeek && it.mealType == mealType
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
