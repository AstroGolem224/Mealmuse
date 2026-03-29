package com.mealmuse.feature.recipebook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun RecipeBookScreen(
    modifier: Modifier = Modifier,
    viewModel: RecipeBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cookbook") },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorites() }) {
                        Icon(
                            if (uiState.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            uiState.error?.let { error ->
                ErrorCard(message = error, onRetry = { viewModel.clearError() })
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.recipes.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.MenuBook,
                        title = "No recipes yet",
                        subtitle = "Create your first recipe or generate a meal plan to get started",
                        actionLabel = "Create Recipe",
                        onAction = { showCreateDialog = true }
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                title = recipe.name,
                                subtitle = "${recipe.totalTimeMinutes} min • ${recipe.servings} servings",
                                imageUrl = recipe.imageUrl,
                                calories = recipe.calories.toInt(),
                                onClick = { /* Navigate to detail */ }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRecipeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, instructions, prep, cook, servings ->
                viewModel.createRecipe(name, desc, instructions, prep, cook, servings)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateRecipeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, instructions: List<String>, prepTime: Int, cookTime: Int, servings: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Recipe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (one step per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cook (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            description,
                            instructions.split("\n").filter { it.isNotBlank() },
                            prepTime.toIntOrNull() ?: 0,
                            cookTime.toIntOrNull() ?: 0,
                            servings.toIntOrNull() ?: 1
                        )
                    }
                }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
