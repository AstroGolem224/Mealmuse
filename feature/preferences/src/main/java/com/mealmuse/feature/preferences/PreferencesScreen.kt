package com.mealmuse.feature.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mealmuse.domain.model.DietaryMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Preferences") }) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dietary Mode Selector
            Text("Dietary Mode", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val modes = listOf(
                    DietaryMode.Keto to "Keto",
                    DietaryMode.LowCarb to "Low-Carb",
                    DietaryMode.Vegetarian to "Vegetarian",
                    DietaryMode.Vegan to "Vegan",
                    DietaryMode.Paleo to "Paleo",
                    DietaryMode.CalorieDeficit to "Calorie Deficit"
                )
                items(modes) { (mode, label) ->
                    FilterChip(
                        selected = uiState.preferences.dietaryModes.contains(mode),
                        onClick = { viewModel.selectDietaryMode(mode) },
                        label = { Text(label) }
                    )
                }
            }

            Divider()

            // Macro Settings
            Text("Daily Targets", style = MaterialTheme.typography.titleMedium)

            Text("Max Calories: ${uiState.preferences.maxCalories} kcal")
            Slider(
                value = uiState.preferences.maxCalories.toFloat(),
                onValueChange = { viewModel.updateMaxCalories(it.toInt()) },
                valueRange = 1000f..3500f,
                steps = 24
            )

            Text("Min Protein: ${uiState.preferences.minProtein}g")
            Slider(
                value = uiState.preferences.minProtein.toFloat(),
                onValueChange = { viewModel.updateMinProtein(it.toInt()) },
                valueRange = 20f..200f,
                steps = 17
            )

            Text("Max Carbs: ${uiState.preferences.maxCarbs}g")
            Slider(
                value = uiState.preferences.maxCarbs.toFloat(),
                onValueChange = { viewModel.updateMaxCarbs(it.toInt()) },
                valueRange = 20f..400f,
                steps = 37
            )
        }
    }
}
