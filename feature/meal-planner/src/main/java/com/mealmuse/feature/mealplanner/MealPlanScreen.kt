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

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private val DURATION_OPTIONS = listOf(
    1 to "1 Day",
    3 to "3 Days",
    7 to "1 Week",
    14 to "2 Weeks"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableIntStateOf(1) }

    // Reset to day 1 whenever duration changes
    LaunchedEffect(uiState.selectedDuration) {
        selectedDay = 1
    }

    // Day labels and count for current duration
    val dayLabels = when (uiState.selectedDuration) {
        1 -> listOf("Today")
        3 -> listOf("Day 1", "Day 2", "Day 3")
        else -> DAY_LABELS
    }
    // For 14-day plans week 2 maps days 8–14; effectiveDay accounts for weekOffset
    val effectiveDay = selectedDay + uiState.weekOffset * 7

    // Only show week selector for 2-week plans
    val showWeekSelector = uiState.selectedDuration >= 14

    val emptySubtitle = when (uiState.selectedDuration) {
        1 -> "Generate a personalized meal plan for today"
        3 -> "Generate a 3-day meal plan based on your preferences"
        14 -> "Generate a 2-week meal plan — split into daily chunks for speed"
        else -> "Generate a personalized weekly meal plan based on your preferences"
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!uiState.isGenerating) viewModel.generatePlan() },
                containerColor = if (uiState.isGenerating)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isGenerating) {
                    Icon(Icons.Default.Refresh, contentDescription = "Generating...")
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
            // Duration selector
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DURATION_OPTIONS) { (days, label) ->
                    FilterChip(
                        selected = uiState.selectedDuration == days,
                        onClick = { viewModel.setDuration(days) },
                        label = { Text(label) },
                        enabled = !uiState.isGenerating
                    )
                }
            }

            // Week selector (only for 2-week plans)
            if (showWeekSelector) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousWeek() },
                        enabled = uiState.weekOffset > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
                    }
                    Text(
                        "Week ${uiState.weekOffset + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = { viewModel.nextWeek() },
                        enabled = uiState.weekOffset < 1
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next week")
                    }
                }
            }

            // Day selector
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayLabels.indices.toList()) { index ->
                    val day = index + 1
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(dayLabels[index]) }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                uiState.generationProgress ?: "Generating your meal plan...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (uiState.selectedDuration > 3) {
                                Text(
                                    "Generating in chunks for reliability",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                uiState.currentPlan == null -> {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No meal plan yet",
                        subtitle = emptySubtitle,
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
                            val entry = viewModel.getEntry(effectiveDay, mealType)
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
