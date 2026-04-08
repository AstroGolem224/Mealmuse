package com.mealmuse.app

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mealmuse.domain.model.LLMSettings
import com.mealmuse.domain.repository.LLMRepository
import com.mealmuse.feature.mealplanner.MealPlanScreen
import com.mealmuse.feature.recipebook.RecipeBookScreen
import com.mealmuse.feature.recipebook.RecipeDetailScreen
import com.mealmuse.feature.fridge.FridgeScreen
import com.mealmuse.feature.aisuggest.AISuggestScreen
import com.mealmuse.feature.onboarding.OnboardingScreen
import com.mealmuse.feature.preferences.PreferencesScreen
import com.mealmuse.feature.settings.SettingsScreen
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.collectAsState

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object MealPlan : Screen("meal_plan", "Meal Plan", Icons.Default.CalendarMonth)
    data object Cookbook : Screen("cookbook", "Cookbook", Icons.Default.MenuBook)
    data object Fridge : Screen("fridge", "Fridge", Icons.Default.Kitchen)
}

private val bottomNavItems = listOf(
    Screen.MealPlan,
    Screen.Cookbook,
    Screen.Fridge
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    llmRepository: LLMRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("mealmuse_prefs", Context.MODE_PRIVATE) }
    val onboardingCompleted = remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }

    val showBottomBar = remember(currentDestination) {
        bottomNavItems.any { currentDestination?.hierarchy?.any { dest ->
            dest.route == it.route
        } == true }
    }

    val currentRoute = currentDestination?.route

    // Reactive LLM settings for FAB visibility
    val llmSettings by llmRepository.getLLMSettingsFlow().collectAsState(
        initial = LLMSettings(
            provider = com.mealmuse.domain.model.LLMProvider.OPENAI,
            apiKey = "",
            model = "gpt-4o-mini",
            isActive = false
        )
    )

    Scaffold(
        topBar = {
            if (showBottomBar && onboardingCompleted.value) {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                Screen.MealPlan.route -> "Meal Plan"
                                Screen.Cookbook.route -> "Cookbook"
                                Screen.Fridge.route -> "Fridge"
                                else -> "MealMuse"
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("preferences") }) {
                            Icon(Icons.Default.Tune, contentDescription = "Preferences")
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar && onboardingCompleted.value) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (onboardingCompleted.value) {
                val currentRoute = currentDestination?.route
                if (currentRoute == Screen.Cookbook.route || currentRoute == Screen.Fridge.route) {
                    if (llmSettings.isActive && llmSettings.apiKey.isNotBlank()) {
                        FloatingActionButton(onClick = {
                            navController.navigate("ai_suggest")
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suggest")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted.value) Screen.MealPlan.route else "onboarding",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onComplete = {
                        prefs.edit().putBoolean("onboarding_completed", true).apply()
                        onboardingCompleted.value = true
                        navController.navigate(Screen.MealPlan.route) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
    composable(Screen.MealPlan.route) {
        MealPlanScreen(
            onRecipeClick = { recipeId ->
                navController.navigate("recipe_detail/$recipeId")
            }
        )
    }
            composable(Screen.Cookbook.route) {
                RecipeBookScreen(
                    onRecipeClick = { recipeId ->
                        navController.navigate("recipe_detail/$recipeId")
                    }
                )
            }
            composable(Screen.Fridge.route) {
                FridgeScreen()
            }
            composable("ai_suggest") {
                AISuggestScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("preferences") {
                PreferencesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "recipe_detail/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                RecipeDetailScreen(
                    recipeId = recipeId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
