package com.mealmuse.feature.aisuggest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mealmuse.core.ui.EmptyState
import com.mealmuse.core.ui.ErrorCard
import com.mealmuse.core.ui.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISuggestScreen(
    modifier: Modifier = Modifier,
    viewModel: AISuggestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Suggest") }) }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(selectedTabIndex = uiState.activeTab) {
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Research") }
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Improve") }
                )
            }

            when (uiState.activeTab) {
                0 -> ResearchTab(searchQuery, uiState, viewModel) { searchQuery = it }
                1 -> ImproveTab(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun ResearchTab(
    searchQuery: String,
    uiState: AISuggestUiState,
    viewModel: AISuggestViewModel,
    onQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search recipes...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(onClick = { viewModel.researchRecipes(searchQuery) }) {
                    Icon(Icons.Default.AutoAwesome, "Research")
                }
            },
            singleLine = true
        )

        uiState.error?.let { ErrorCard(message = it, onRetry = { viewModel.clearError() }) }

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.searchResults.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "Search for recipes",
                    subtitle = "AI will research and rank recipes based on your query"
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.searchResults) { recipe ->
                        RecipeCard(
                            title = recipe.name,
                            subtitle = recipe.description,
                            calories = recipe.calories.toInt(),
                            onClick = { viewModel.saveRecipe(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImproveTab(
    uiState: AISuggestUiState,
    viewModel: AISuggestViewModel
) {
    if (uiState.improvement == null && !uiState.isLoading) {
        EmptyState(
            icon = Icons.Default.AutoFixHigh,
            title = "Improve a recipe",
            subtitle = "Select a recipe from your cookbook to get AI-powered improvement suggestions"
        )
        return
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    uiState.improvement?.let { improvement ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Improved Recipe", style = MaterialTheme.typography.headlineSmall)
                Text("Score: ${improvement.score}/100", style = MaterialTheme.typography.titleMedium)
            }
            item {
                RecipeCard(
                    title = improvement.improvedRecipe.name,
                    subtitle = improvement.improvedRecipe.description,
                    calories = improvement.improvedRecipe.calories.toInt()
                )
            }
            item {
                Button(onClick = { viewModel.saveRecipe(improvement.improvedRecipe) }) {
                    Text("Save Improved Recipe")
                }
            }
            item {
                Text("Changes", style = MaterialTheme.typography.titleMedium)
            }
            items(improvement.changes) { change ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(change.field, style = MaterialTheme.typography.labelMedium)
                        Text("Before: ${change.oldValue}", style = MaterialTheme.typography.bodySmall)
                        Text("After: ${change.newValue}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
