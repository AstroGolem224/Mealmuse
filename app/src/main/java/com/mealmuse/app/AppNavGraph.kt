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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mealmuse.feature.mealplanner.MealPlanScreen
import com.mealmuse.feature.recipebook.RecipeBookScreen
import com.mealmuse.feature.fridge.FridgeScreen
import com.mealmuse.feature.aisuggest.AISuggestScreen
import com.mealmuse.feature.onboarding.OnboardingScreen
import com.mealmuse.feature.preferences.PreferencesScreen
import com.mealmuse.feature.settings.SettingsScreen

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
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("mealmuse_prefs", Context.MODE_PRIVATE)
    val onboardingCompleted = remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }

    val showBottomBar = bottomNavItems.any { currentDestination?.hierarchy?.any { dest ->
        dest.route == it.route
    } == true }

    Scaffold(
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
            if (currentDestination?.route == "cookbook") {
                FloatingActionButton(onClick = {
                    navController.navigate("ai_suggest")
                }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suggest")
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
                MealPlanScreen()
            }
            composable(Screen.Cookbook.route) {
                RecipeBookScreen()
            }
            composable(Screen.Fridge.route) {
                FridgeScreen()
            }
            composable("ai_suggest") {
                AISuggestScreen()
            }
            composable("preferences") {
                PreferencesScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}
