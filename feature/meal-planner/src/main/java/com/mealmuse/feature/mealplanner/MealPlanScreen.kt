package com.mealmuse.feature.mealplanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.mealmuse.core.ui.MealSlot
import com.mealmuse.domain.model.MealType

private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meal Plan") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.generatePlan() },
                containerColor = if (uiState.isGenerating) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Generate Plan")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Week Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousWeek() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
                }
                Text(
                    "Week ${uiState.weekOffset + 1}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { viewModel.nextWeek() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next week")
                }
            }

            // Day Selector
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek.indices.toList()) { index ->
                    val day = index + 1
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(daysOfWeek[index]) }
                    )
                }
            }

            // Error
            uiState.error?.let { error ->
                ErrorCard(
                    message = error,
                    onRetry = { viewModel.generatePlan() },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Content
            when {
                uiState.isGenerating -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Generating your meal plan...")
                        }
                    }
                }
                uiState.currentPlan == null -> {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No meal plan yet",
                        subtitle = "Generate a personalized weekly meal plan based on your preferences and available ingredients",
                        actionLabel = "Generate Plan",
                        onAction = { viewModel.generatePlan() }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(MealType.entries.toList()) { mealType ->
                            val entry = viewModel.getEntry(selectedDay, mealType)
                            MealSlot(
                                mealType = mealType.displayName,
                                recipeName = entry?.recipe?.name ?: "Not planned",
                                onClick = { /* Navigate to recipe */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
