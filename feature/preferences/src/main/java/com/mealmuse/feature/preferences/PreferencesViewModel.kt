package com.mealmuse.feature.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import com.mealmuse.domain.usecase.SetDietaryModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreferencesUiState(
    val preferences: UserPreferences = UserPreferences(dietaryModes = emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val setDietaryModeUseCase: SetDietaryModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    fun selectDietaryMode(mode: DietaryMode) {
        val currentPrefs = _uiState.value.preferences
        when (val result = setDietaryModeUseCase(currentPrefs, mode)) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(preferences = result.data)
            }
            is Result.Failure -> {
                _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
            is Result.Loading -> {}
        }
    }

    fun updateMaxCalories(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(maxCalories = value)
        )
    }

    fun updateMinProtein(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(minProtein = value)
        )
    }

    fun updateMaxCarbs(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(maxCarbs = value)
        )
    }
}
