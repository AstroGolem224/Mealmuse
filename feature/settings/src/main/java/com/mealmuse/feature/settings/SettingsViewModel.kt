package com.mealmuse.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.LLMProvider
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.usecase.ManageLLMSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: LLMSettings = LLMSettings(LLMProvider.OPENAI, "", "gpt-4o-mini", isActive = false),
    val availableModels: List<String> = emptyList(),
    val isValidating: Boolean = false,
    val validationResult: Boolean? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageLLMSettingsUseCase: ManageLLMSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            when (val result = manageLLMSettingsUseCase.getSettings()) {
                is Result.Success -> {
                    val settings = result.data
                    val models = manageLLMSettingsUseCase.getModelsForProvider(settings.provider)
                    _uiState.value = _uiState.value.copy(
                        settings = settings,
                        availableModels = models
                    )
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(error = result.exception.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectProvider(provider: LLMProvider) {
        val models = manageLLMSettingsUseCase.getModelsForProvider(provider)
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(
                provider = provider,
                model = models.firstOrNull() ?: ""
            ),
            availableModels = models,
            validationResult = null
        )
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(apiKey = key),
            validationResult = null
        )
    }

    fun selectModel(model: String) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(model = model)
        )
    }

    fun updateBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(baseUrl = url.ifBlank { null })
        )
    }

    fun validateKey() {
        val settings = _uiState.value.settings
        if (settings.apiKey.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidating = true, validationResult = null)
            when (val result = manageLLMSettingsUseCase.validateKey(settings.provider, settings.apiKey)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = result.data
                )
                is Result.Failure -> _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = false,
                    error = result.exception.message
                )
                is Result.Loading -> {}
            }
        }
    }

    fun saveSettings() {
        val settings = _uiState.value.settings
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = manageLLMSettingsUseCase.saveSettings(settings.copy(isActive = true))) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isSaving = false)
                is Result.Failure -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.exception.message
                )
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
