# Create tasks in PM Tool for MealMuse audit
$projectId = "5f3c8a77-21ba-4bfa-b6e5-dd4a754d02ad"
$columnId = "bf90633c-11a2-4c34-b2e8-7c6558e96ef5"
$baseUri = "http://100.115.61.30:8000/api/tasks"

$tasks = @(
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-4] SaveRecipeUseCase result ignored in RecipeBookViewModel"
        description = "Problem: RecipeBookViewModel.createRecipe calls saveRecipeUseCase(recipe) but does not observe the Result. If saving fails (e.g., database error), no error state is updated and user sees no feedback. This leads to silent data loss. Proposed fix: Modify SaveRecipeUseCase to return Result<Unit> and handle the result in ViewModel, updating error state on failure via _uiState.value = _uiState.value.copy(error = result.exception.message)."
        priority = "high"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-5] AISuggestViewModel doesn't handle recipe selection for improvement"
        description = "Problem: AISuggestViewModel.improveRecipe(recipe, focus) expects a recipe parameter but the UI has no state to track which recipe is selected. This likely breaks the Improve tab flow entirely. Proposed fix: Add selectedRecipe: MutableStateFlow<Recipe?> and selectRecipe(recipe) method. Call improveRecipe using the selected recipe if parameter is null."
        priority = "high"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-9] Missing validation for ingredient quantity input"
        description = "Problem: AddIngredientDialog accepts any string for quantity; invalid numbers convert to 0f silently. Proposed fix: Validate with toFloatOrNull() and show error UI if invalid, preventing submission of zero quantity without user awareness."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-10] No input sanitization for recipe names/descriptions"
        description = "Problem: Recipe creation accepts raw strings without trimming, max length, or content checks, risking UI overflow and prompt injection. Proposed fix: Add validation (trim, max 100 chars, allowed characters) in ViewModel or use case, throw IllegalArgumentException on invalid input."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UI-4] RecipeCard image loading with Coil lacks placeholder/error handling"
        description = "Problem: AsyncImage shows blank space while loading and on error, giving empty-looking cards. Proposed fix: Add placeholder and error drawables to AsyncImage for better feedback."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UX-6] No confirmation after saving settings"
        description = "Problem: saveSettings() updates validationResult to true but UI doesn't show a success message. User sees no feedback after saving. Proposed fix: Use Snackbar with message 'Settings saved successfully' that auto-dismisses after 2 seconds. Observe validationResult in SettingsScreen and show Snackbar via LaunchedEffect."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[FUNC-1] RecipeBookViewModel toggles favorites but UI has no favorite filter"
        description = "Problem: toggleFavorites() exists but UI has no button to toggle favorites; state is unreachable. Proposed fix: Add IconButton (favorite icon) in top bar or search area to toggle favorites, wire to viewModel.toggleFavorites() and reflect active state with tint."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[FUNC-4] AI Suggest Research tab not connected to recipe saving"
        description = "Problem: AI Suggest Research results lack a Save button; user cannot add found recipes to cookbook directly. Proposed fix: Add 'Save' button on each research result card, calling viewModel.saveRecipe(recipe) and showing confirmation."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[FUNC-8] No API key masking in UI"
        description = "Problem: API key visible as plain text in Settings UI; shoulder surfing risk. Proposed fix: Use VisualTransformation.Password with eye icon toggle to show/hide."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[FUNC-9] Dietary mode auto-configuration missing"
        description = "Problem: If user selects Keto, macros should be set to keto ranges automatically; current code may not auto-set macros for preset modes. Proposed fix: In SetDietaryModeUseCase, when a preset mode is selected, also update UserPreferences macros to mode's recommended values (e.g., Keto: maxCarbs=50, minProtein=...). Provide way to customize after."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UX-2] Generate Meal Plan FAB shows spinning icon but no detailed progress"
        description = "Problem: FAB shows spinning but no detailed progress; user may tap repeatedly. Proposed fix: Show progress text next to FAB or in a Snackbar. Disable FAB during generation. Add onChunkComplete updates to show Snackbar with message 'Completed part X of Y'."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UI-1] Missing accessibility labels in FridgeScreen ingredient item"
        description = "Problem: IngredientItem card lacks semantic merging; screen readers cannot convey full ingredient info. Proposed fix: Wrap Card content in Semantics with combined contentDescription including name, quantity, unit, category, and expiry status."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UI-2] Inconsistent button text language (German/English mix)"
        description = "Problem: ErrorCard uses German text like 'Fehler' and 'Erneut versuchen' while other UI uses English. Proposed fix: Standardize on English: change to 'Error' and 'Retry'."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[SETTINGS-1] No reset-to-defaults for preferences/settings"
        description = "Problem: No reset-to-defaults; user must manually revert each setting. Proposed fix: Add 'Reset to Defaults' button in Preferences and Settings screens to restore default UserPreferences (2000 cal, 50 protein, etc.) and default LLM settings, then save."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    # Tier 2
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UX-1] No confirmation before destructive actions"
        description = "Problem: Deleting recipe or ingredient is immediate with no confirmation dialog, leading to accidental data loss. Proposed fix: Add AlertDialog on delete request with message 'Are you sure? This cannot be undone.' and confirm/cancel buttons."
        priority = "high"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-3] FridgeViewModel filterByCategory duplicates collection logic"
        description = "Problem: filterByCategory re-launches flow collection and filters locally, while loadIngredients() also collects same flow; multiple collectors cause redundant updates and potential leaks. Proposed fix: Have ManageFridgeUseCase provide getIngredientsByCategory(category) returning pre-filtered Flow, and maintain single collection job."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-6] LLMProviderFactory missing"
        description = "Problem: LLMProvider classes exist but no factory to instantiate correct provider based on settings, leading to conditional logic or missing functionality. Proposed fix: Create LLMProviderFactory object with create(provider, apiKey, baseUrl) returning appropriate provider instance."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[SETTINGS-2] Settings do not apply immediately; require restart"
        description = "Problem: Settings do not apply immediately; require app restart. Proposed fix: Expose LLMSettings as StateFlow from LLMRepository and collect in UI for automatic recomposition on changes."
        priority = "high"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[SETTINGS-7] Model selection limited; dynamic fetch not auto-triggered"
        description = "Problem: refreshModels() exists but UI lacks button; user cannot fetch available models dynamically and not auto-triggered on provider change. Proposed fix: Add 'Refresh Models' button in Settings; call refreshModels() on provider select automatically and show loading."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[FUNC-5] Recipe detail navigation missing from MealPlan slots"
        description = "Problem: MealSlot onClick is empty; recipe detail navigation missing from MealPlan. User cannot view full recipe from plan. Proposed fix: Implement onClick to navigate to recipe_detail/${entry.recipe.id} with null safety."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-1] Race condition in GenerateMealPlanUseCase chunk handling"
        description = "Problem: mutable allEntries list shared across chunks and no cancellation token; rapid generate calls could corrupt state. Proposed fix: Use immutable list with flatMap, add currentJob cancellation."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[LOGIC-7] LLM settings stored in two places (SharedPreferences vs Room)"
        description = "Problem: LLMSettings stored in both SharedPreferences and Room, leading to inconsistency. Proposed fix: Choose single source (prefer Room) and ensure all repository access uses that store exclusively."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UX-4] No offline detection or network error handling"
        description = "Problem: Network failures show generic error; no offline detection. Proposed fix: Implement connectivity monitoring; detect IOException in providers; show message 'No internet connection. Check your network.'; add offline banner."
        priority = "high"
        status = "backlog"
        agent = "Forge"
    },
    [PSCustomObject]@{
        project_id = $projectId
        column_id = $columnId
        title = "[UX-5] Settings screen lacks model selection UX refresh"
        description = "Problem: Settings screen lacks button to trigger refreshModels; static model list. Proposed fix: Add 'Refresh Models' button next to dropdown; already implemented in ViewModel but not surfaced."
        priority = "medium"
        status = "backlog"
        agent = "Forge"
    }
)

foreach ($task in $tasks) {
    try {
        $body = $task | ConvertTo-Json -Depth 10
        $response = Invoke-RestMethod -Uri $baseUri -Method Post -Body $body -ContentType "application/json"
        Write-Host "Created task: $($task.title)"
    } catch {
        Write-Error "Failed to create task $($task.title): $_"
    }
}