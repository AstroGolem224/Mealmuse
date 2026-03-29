package com.mealmuse.feature.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Ingredient
import com.mealmuse.domain.model.IngredientCategory
import com.mealmuse.domain.usecase.ManageFridgeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FridgeUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val expiringSoon: List<Ingredient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: IngredientCategory? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val manageFridgeUseCase: ManageFridgeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridgeUiState())
    val uiState: StateFlow<FridgeUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
        loadExpiringSoon()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            manageFridgeUseCase.getAllIngredients().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Result.Success -> _uiState.value = _uiState.value.copy(
                        ingredients = result.data,
                        isLoading = false,
                        error = null
                    )
                    is Result.Failure -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
            }
        }
    }

    private fun loadExpiringSoon() {
        viewModelScope.launch {
            manageFridgeUseCase.getExpiringSoon().collect { result ->
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(expiringSoon = result.data)
                }
            }
        }
    }

    fun addIngredient(name: String, quantity: Float, unit: String, category: IngredientCategory, expiryDate: Long? = null) {
        viewModelScope.launch {
            val result = manageFridgeUseCase.addIngredient(name, quantity, unit, category, expiryDate)
            if (result is Result.Failure) {
                _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }

    fun deleteIngredient(id: String) {
        viewModelScope.launch {
            val result = manageFridgeUseCase.deleteIngredient(id)
            if (result is Result.Failure) {
                _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }

    fun filterByCategory(category: IngredientCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        if (category != null) {
            viewModelScope.launch {
                manageFridgeUseCase.getAllIngredients().collect { result ->
                    if (result is Result.Success) {
                        _uiState.value = _uiState.value.copy(
                            ingredients = result.data.filter { it.category == category }
                        )
                    }
                }
            }
        } else {
            loadIngredients()
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadIngredients()
            return
        }
        viewModelScope.launch {
            manageFridgeUseCase.searchIngredients(query).collect { result ->
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(ingredients = result.data)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}