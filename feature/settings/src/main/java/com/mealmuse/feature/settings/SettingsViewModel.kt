package com.mealmuse.feature.settings

import android.util.Log
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
    val isRefreshingModels: Boolean = false,
    val error: String? = null,
    val isLoaded: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageLLMSettingsUseCase: ManageLLMSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SettingsVM"
    }

    init {
        Log.d(TAG, "Initializing SettingsViewModel")
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            Log.d(TAG, "Loading settings...")
            when (val result = manageLLMSettingsUseCase.getSettings()) {
                is Result.Success -> {
                    val settings = result.data
                    val models = manageLLMSettingsUseCase.getModelsForProvider(settings.provider)
                    Log.d(TAG, "Settings loaded: provider=${settings.provider}, model=${settings.model}, apiKey=${if(settings.apiKey.isBlank()) "EMPTY" else "SET"}")
                    _uiState.value = _uiState.value.copy(
                        settings = settings,
                        availableModels = models,
                        isLoaded = true
                    )
                }
                is Result.Failure -> {
                    Log.e(TAG, "Failed to load settings: ${result.exception.message}")
                    val models = manageLLMSettingsUseCase.getModelsForProvider(LLMProvider.OPENAI)
                    _uiState.value = _uiState.value.copy(
                        availableModels = models,
                        isLoaded = true,
                        error = "Using default settings. Please configure your API key."
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun selectProvider(provider: LLMProvider) {
        val models = manageLLMSettingsUseCase.getModelsForProvider(provider)
        Log.d(TAG, "Provider selected: $provider, models: ${models.take(3)}")
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
        Log.d(TAG, "API key updated: ${if(key.isBlank()) "EMPTY" else "SET"}")
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

    fun refreshModels() {
        val provider = _uiState.value.settings.provider
        val apiKey = _uiState.value.settings.apiKey
        val currentModel = _uiState.value.settings.model
        _uiState.value = _uiState.value.copy(isRefreshingModels = true, error = null)

        viewModelScope.launch {
            try {
                val models = manageLLMSettingsUseCase.fetchModelsForProvider(provider, apiKey)
                val selectedModel = if (currentModel.isNotBlank() && models.contains(currentModel)) currentModel
                    else models.firstOrNull() ?: currentModel

                _uiState.value = _uiState.value.copy(
                    availableModels = models,
                    isRefreshingModels = false,
                    settings = _uiState.value.settings.copy(model = selectedModel)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshingModels = false,
                    error = "Failed to refresh models: ${e.message}"
                )
            }
        }
    }

    fun validateKey() {
        val settings = _uiState.value.settings
        if (settings.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                validationResult = false,
                error = "Please enter an API key first"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isValidating = true, error = null)

        viewModelScope.launch {
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
        Log.d(TAG, "Saving settings: provider=${settings.provider}, model=${settings.model}, apiKeySet=${settings.apiKey.isNotBlank()}")
        
        if (settings.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter an API key")
            return
        }
        
        if (settings.model.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please select a model")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, validationResult = null)
            when (val result = manageLLMSettingsUseCase.saveSettings(settings.copy(isActive = true))) {
                is Result.Success -> {
                    Log.d(TAG, "Settings saved successfully!")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        validationResult = true
                    )
                }
                is Result.Failure -> {
                    Log.e(TAG, "Failed to save settings: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.exception.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetSettings() {
        val defaults = LLMSettings(
            provider = LLMProvider.OPENAI,
            apiKey = "",
            model = "gpt-4o-mini",
            isActive = false
        )
        _uiState.value = _uiState.value.copy(
            settings = defaults,
            availableModels = manageLLMSettingsUseCase.getModelsForProvider(LLMProvider.OPENAI),
            validationResult = null,
            error = null
        )
        // Save empty defaults
        viewModelScope.launch {
            manageLLMSettingsUseCase.saveSettings(defaults)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
