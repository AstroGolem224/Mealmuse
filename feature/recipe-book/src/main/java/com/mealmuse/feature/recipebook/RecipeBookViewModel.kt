package com.mealmuse.feature.recipebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmuse.core.common.Result
import com.mealmuse.core.common.generateUUID
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.usecase.SaveRecipeUseCase
import com.mealmuse.domain.usecase.SearchRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeBookUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false
)

@HiltViewModel
class RecipeBookViewModel @Inject constructor(
    private val searchRecipeUseCase: SearchRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val recipeRepository: com.mealmuse.domain.repository.RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeBookUiState())
    val uiState: StateFlow<RecipeBookUiState> = _uiState.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchRecipeUseCase("").collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Result.Success -> _uiState.value = _uiState.value.copy(
                        recipes = result.data,
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

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                searchRecipeUseCase(query).collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        is Result.Success -> _uiState.value = _uiState.value.copy(
                            recipes = result.data,
                            isLoading = false,
                            error = null
                        )
                        is Result.Failure -> _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
            } catch (e: Exception) {
                // Catch any unexpected exception from the flow collection
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleFavorites() {
        _uiState.value = _uiState.value.copy(showFavoritesOnly = !_uiState.value.showFavoritesOnly)
        if (_uiState.value.showFavoritesOnly) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                recipeRepository.getFavorites().collect { result ->
                    if (result is Result.Success) {
                        _uiState.value = _uiState.value.copy(recipes = result.data)
                    }
                }
            }
        } else {
            loadRecipes()
        }
    }

    fun createRecipe(name: String, description: String, instructions: List<String>, prepTime: Int, cookTime: Int, servings: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val now = System.currentTimeMillis()
            val recipe = Recipe(
                id = generateUUID(),
                name = name.trim(),
                description = description.trim(),
                instructions = instructions,
                prepTimeMinutes = prepTime,
                cookTimeMinutes = cookTime,
                servings = servings,
                calories = 0f,
                protein = 0f,
                carbs = 0f,
                fat = 0f,
                createdAt = now,
                updatedAt = now
            )
            when (val result = saveRecipeUseCase(recipe)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Optionally refresh list could be triggered here
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to create recipe"
                    )
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(id)
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            recipeRepository.toggleFavorite(id)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
