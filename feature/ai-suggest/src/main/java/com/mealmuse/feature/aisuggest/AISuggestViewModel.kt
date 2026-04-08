package com.mealmuse.feature.aisuggest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.RecipeImprovement
import com.mealmuse.domain.usecase.ImproveRecipeUseCase
import com.mealmuse.domain.usecase.ResearchRecipeUseCase
import com.mealmuse.domain.usecase.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AISuggestUiState(
    val searchResults: List<Recipe> = emptyList(),
    val improvement: RecipeImprovement? = null,
    val isLoading: Boolean = false,
    val activeTab: Int = 0,
    val error: String? = null,
    val selectedRecipe: Recipe? = null
)

@HiltViewModel
class AISuggestViewModel @Inject constructor(
    private val researchRecipeUseCase: ResearchRecipeUseCase,
    private val improveRecipeUseCase: ImproveRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISuggestUiState())
    val uiState: StateFlow<AISuggestUiState> = _uiState.asStateFlow()

    fun researchRecipes(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = researchRecipeUseCase(query)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    searchResults = result.data,
                    isLoading = false
                )
                is Result.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exception.message
                )
                is Result.Loading -> {}
            }
        }
    }

    fun improveRecipe(recipe: Recipe, focus: String = "health") {
        selectRecipe(recipe)
        improveRecipe(focus)
    }

    fun improveRecipe(focus: String = "health") {
        val selected = _uiState.value.selectedRecipe
        if (selected == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a recipe first")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = improveRecipeUseCase(selected, focus)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    improvement = result.data,
                    isLoading = false
                )
                is Result.Failure -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exception.message
                )
                is Result.Loading -> {}
            }
        }
    }

    fun selectRecipe(recipe: Recipe?) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            saveRecipeUseCase(recipe)
        }
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
