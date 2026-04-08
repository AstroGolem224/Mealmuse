package com.mealmuse.feature.preferences

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.DietaryMode
import com.mealmuse.domain.model.UserPreferences
import com.mealmuse.domain.usecase.SetDietaryModeUseCase
import com.mealmuse.domain.usecase.ManageUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreferencesUiState(
    val preferences: UserPreferences = UserPreferences(dietaryModes = listOf(DietaryMode.Keto)),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val setDietaryModeUseCase: SetDietaryModeUseCase,
    private val manageUserPreferencesUseCase: ManageUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            manageUserPreferencesUseCase.getPreferences().collectLatest { prefs ->
                _uiState.value = _uiState.value.copy(
                    preferences = prefs,
                    isLoading = false
                )
            }
        }
    }

    fun selectDietaryMode(mode: DietaryMode) {
        val currentPrefs = _uiState.value.preferences
        when (val result = setDietaryModeUseCase(currentPrefs, mode)) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(preferences = result.data, isSaved = false)
            }
            is Result.Failure -> {
                _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
            is Result.Loading -> {}
        }
    }

    fun updateMaxCalories(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(maxCalories = value),
            isSaved = false
        )
    }

    fun updateMinProtein(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(minProtein = value),
            isSaved = false
        )
    }

    fun updateMaxCarbs(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(maxCarbs = value),
            isSaved = false
        )
    }

    fun updateMaxFat(value: Int) {
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(maxFat = value),
            isSaved = false
        )
    }

    fun savePreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = manageUserPreferencesUseCase.savePreferences(_uiState.value.preferences)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        error = null
                    )
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to save: ${result.exception.message}"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetPreferences() {
        val defaults = UserPreferences(
            dietaryModes = listOf(DietaryMode.Keto),
            maxCalories = 2000,
            minProtein = 50,
            maxCarbs = 300,
            maxFat = 70
        )
        _uiState.value = _uiState.value.copy(preferences = defaults, isSaved = false)
        // Auto-save
        viewModelScope.launch {
            manageUserPreferencesUseCase.savePreferences(defaults)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
