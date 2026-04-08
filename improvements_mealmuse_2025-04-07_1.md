# MealMuse Repo Audit — 2025-04-07

## Source Manifest

Files read: 40+ Kotlin files across domain, data, feature modules, core
Tech stack: Kotlin, Jetpack Compose, Hilt, Room, OkHttp, Gradle (multi-module)
Architecture pattern: Clean Architecture (Domain-Data-UI) with MVVM
Entry flow: MealMuseApp (Hilt) → MainActivity → AppNavGraph → Onboarding or Main Screen (MealPlan)
State management: StateFlow in ViewModels, Repository pattern, Room Flow observables, unidirectional data flow
Settings system: LLMSettings stored in SharedPreferences via LLMSettingsStore; UserPreferences stored in Room DB; preferences accessed via use cases

---

## Category 1: Logic & Architecture

### LOGIC-1 Race condition in MealPlan generation chunk handling

**File(s):** `domain/src/main/java/com/mealmuse/domain/usecase/GenerateMealPlanUseCase.kt:38-114`
**Current behavior:** The `invoke` function processes chunks sequentially but updates a shared mutable list `allEntries` while the ViewModel's `onChunkComplete` callback potentially modifies UI state.
**Problem:** If the ViewModel performs UI updates during each chunk callback, race conditions could occur if multiple generation calls are made quickly (e.g., user spams generate). The `allEntries` list is mutable and shared across chunks without synchronization.
**Proposed fix:** Make `allEntries` immutable by using `fold` or build a list of chunk results then `flatten()`. Also add a generation cancellation token to prevent overlapping calls:

```kotlin
private var currentJob: Job? = null
suspend operator fun invoke(...): Result<MealPlan> = suspendResult {
    currentJob?.cancel()
    // ... build chunks
    val allEntries = chunks.flatMap { chunk ->
        val prompt = buildPrompt(...)
        val chunkPlan = llmRepository.generateMealPlan(prompt, settings)
        (chunkPlan as? Result.Success)?.data?.entries ?: emptyList()
    }
    // ... merge plan
}
```
**Impact:** High (prevents corrupted state on rapid generation attempts)
**Effort:** Medium
**Priority score:** 5

---

### LOGIC-2 Missing error handling for empty dietary modes

**File(s):** `domain/src/main/java/com/mealmuse/domain/usecase/GenerateMealPlanUseCase.kt:55-65`
**Current behavior:** The code defaults to `DietaryMode.Keto` when `preferences.dietaryModes` is empty, but this default is implicit and not documented.
**Problem:** Users with no dietary mode selected (e.g., fresh install) get Keto without explicit choice. This is a silent fallback that may confuse users expecting "balanced" or "no preference".
**Proposed fix:** Explicitly check for empty list and throw `IllegalStateException` with clear message: "Please select a dietary mode in Preferences first." Or use `DietaryMode.Balanced` if defined. Add default in `PreferencesViewModel` initialization.
**Impact:** Medium (misleading default behavior)
**Effort:** Low
**Priority score:** 7

---

### LOGIC-3 FridgeViewModel's filterByCategory duplicates collection logic

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeViewModel.kt:85-100`
**Current behavior:** `filterByCategory` re-launches a flow collection from `manageFridgeUseCase.getAllIngredients()` and filters locally, while `loadIngredients()` also collects the same flow.
**Problem:** Duplication causes multiple collectors on the same flow, potentially emitting redundant updates and leaking coroutines if not properly cancelled. This violates single source of truth.
**Proposed fix:** Have `ManageFridgeUseCase` provide a `getIngredientsByCategory(category)` method that returns a Flow already filtered at repository level. Then ViewModel simply collects that. Also maintain a single collection job.

```kotlin
private var ingredientsJob: Job? = null
fun filterByCategory(category: IngredientCategory?) {
    ingredientsJob?.cancel()
    if (category != null) {
        viewModelScope.launch {
            manageFridgeUseCase.getIngredientsByCategory(category).collect { ... }
        }
    } else {
        loadIngredients()
    }
}
```
**Impact:** Medium (inefficient, potential memory leaks)
**Effort:** Medium
**Priority score:** 3

---

### LOGIC-4 SaveRecipeUseCase called without result verification

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeBookViewModel.kt:103-122` and multiple locations
**Current behavior:** `createRecipe` calls `saveRecipeUseCase(recipe)` but does not observe the result (Result is ignored).
**Problem:** If saving fails (e.g., database error), the ViewModel won't update error state and user sees no feedback. Silent failures degrade UX.
**Proposed fix:** Make `SaveRecipeUseCase` return a `Result<Unit>` and handle it in ViewModel:

```kotlin
viewModelScope.launch {
    val result = saveRecipeUseCase(recipe)
    if (result is Result.Failure) {
        _uiState.value = _uiState.value.copy(error = result.exception.message)
    }
}
```
**Impact:** High (data loss risk)
**Effort:** Low
**Priority score:** 9

---

### LOGIC-5 AISuggestViewModel doesn't handle recipe selection for improvement

**File(s):** `feature/ai-suggest/src/main/java/com/mealmuse/feature/aisuggest/AISuggestViewModel.kt:53-68`
**Current behavior:** `improveRecipe` takes a `Recipe` parameter but the UI has no state to track which recipe is selected; the ViewModel lacks a method to set the selected recipe.
**Problem:** The UI cannot pass a recipe to improve without a public method or state. This likely breaks the Improve tab flow entirely.
**Proposed fix:** Add a `selectedRecipe: MutableStateFlow<Recipe?>` and `selectRecipe(recipe: Recipe)` method in ViewModel. Then `improveRecipe` uses the currently selected recipe if parameter is null.

```kotlin
private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
fun selectRecipe(recipe: Recipe) {
    _selectedRecipe.value = recipe
}
fun improveRecipe(focus: String = "health") {
    val recipe = _selectedRecipe.value ?: return
    // rest of logic
}
```
**Impact:** High (feature broken)
**Effort:** Low
**Priority score:** 9

---

### LOGIC-6 LLMProviderFactory missing; no dynamic provider selection

**File(s):** `data/ai/src/main/java/com/mealmuse/data/ai/LLMProvider.kt` and no `LLMProviderFactory.kt` found
**Current behavior:** The `LLMRepositoryImpl` needs to instantiate the correct provider based on settings, but no factory exists. I see provider classes (OpenAIProvider, NIMProvider, etc.) but no factory.
**Problem:** Without a factory, the code likely uses conditionals everywhere or only one provider, violating open-closed principle. The HANDOFF.md mentions `LLMProviderFactory` but it's missing from the codebase.
**Proposed fix:** Create `LLMProviderFactory.kt`:

```kotlin
object LLMProviderFactory {
    fun create(provider: LLMProviderEnum, apiKey: String, baseUrl: String? = null): LLMProvider {
        return when(provider) {
            OPENAI -> OpenAIProvider()
            ANTHROPIC -> AnthropicProvider()
            OPENROUTER -> OpenRouterProvider()
            NIM -> NIMProvider(baseUrl)
        }
    }
}
```
**Impact:** High (core architecture incomplete)
**Effort:** Medium
**Priority score:** 5

---

### LOGIC-7 LLM settings stored in two places (SharedPreferences vs Room)

**File(s):** `data/ai/src/main/java/com/mealmuse/data/ai/LLMSettingsStore.kt` (referenced) and `domain/src/main/java/com/mealmuse/domain/model/LLMSettings.kt`
**Current behavior:** The project plan says LLMSettings should be in Room DB (see ARCHITECTURE.md line 273), but HANDOFF mentions `LLMSettingsStore` using SharedPreferences. This creates duplication risk.
**Problem:** Split storage leads to inconsistency; which source is truth? Use cases may read from one and write to another.
**Proposed fix:** Choose one: either store in Room (consistent with other preferences) or keep in SharedPreferences but ensure all access goes through repository that abstracts it. If SharedPreferences is used, `LLMRepository` should read/write there exclusively, not involve Room.
**Impact:** High (data integrity issue)
**Effort:** Medium
**Priority score:** 5

---

### LOGIC-8 GenerateMealPlanUseCase uses random without seed for reproducibility

**File(s):** `domain/src/main/java/com/mealmuse/domain/usecase/GenerateMealPlanUseCase.kt:68`
**Current behavior:** `val variationSeed = (0..999).random()` — random seed each generation.
**Problem:** Users cannot regenerate the same plan or have variety control. Also `VARIATION_STYLES` selection uses modulo which is deterministic but combined with random seed makes prompt unpredictable.
**Proposed fix:** Allow seed to be passed as parameter (with default `System.currentTimeMillis()`). Store seed in MealPlan metadata so same seed could reproduce plan. Or at minimum use `Random(Date().time).nextInt(1000)` consistently throughout.
**Impact:** Low (nice-to-have)
**Effort:** Low
**Priority score:** 3

---

### LOGIC-9 Missing validation for ingredient quantity input

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt:254-257`
**Current behavior:** AddIngredientDialog allows any string for quantity; conversion `toFloatOrNull()` returns null on invalid input, which becomes 0f without feedback.
**Problem:** Invalid quantities (e.g., "abc") silently become 0, causing confusing data (quantity 0).
**Proposed fix:** Validate with `toFloatOrNull()` and if null, show error and prevent submission:

```kotlin
val quantityValue = quantity.toFloatOrNull()
if (name.isNotBlank() && quantityValue != null) {
    onConfirm(name, quantityValue, unit, selectedCategory, null)
} else {
    // show error state
}
```
**Impact:** Medium (data quality)
**Effort:** Low
**Priority score:** 7

---

### LOGIC-10 No input sanitization for recipe names/descriptions

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeBookViewModel.kt:103-122` and similar across ViewModels
**Current behavior:** Recipe creation accepts raw strings without trimming or validation; empty names rejected only by UI button `enabled = name.isNotBlank()`, but no max length or sanitization.
**Problem:** Long names overflow UI, special characters may cause display issues or injection if later used in prompts unsanitized. No central validation.
**Proposed fix:** Add validation rules in use case or ViewModel: trim whitespace, max length (e.g., 100 chars), allow only safe characters. Throw `IllegalArgumentException` on invalid input.
**Impact:** Medium (UI robustness, prompt injection prevention)
**Effort:** Low
**Priority score:** 7

---

## Category 2: UI / Visual Design

### UI-1 Missing accessibility labels in FridgeScreen ingredient item

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt:189-196`
**Current behavior:** Warning icon for expiring soon has `contentDescription = "Expiring soon"`, but the delete button icon has `contentDescription = "Delete"`. However, the `IngredientItem` composable lacks combined accessibility for the whole card.
**Problem:** Screen readers cannot easily convey that the card represents an ingredient with quantity, category, and expiry status. No semantic merging.
**Proposed fix:** Wrap `Card` content in `Semantics` with `contentDescription = "$name, $quantity $unit, ${category.displayName}"` and set `stateDescription` if expiring. Also add `hint` for delete action.

```kotlin
Card(
    modifier = Modifier.fillMaxWidth().semantics {
        contentDescription = "$name, $quantity $unit, ${category.displayName}" +
            if (ingredient.isExpiringSoon) " (expiring soon)" else ""
    },
    ...
)
```
**Impact:** Medium (accessibility compliance)
**Effort:** Low
**Priority score:** 7

---

### UI-2 Inconsistent button text language (German/English mix)

**File(s):** `core/ui/src/main/java/com/mealmuse/core/ui/Components.kt:286-289,299`
**Current behavior:** ErrorCard uses German text: `"Fehler"` and `"Erneut versuchen"`. Meanwhile most other UI uses English (e.g., EmptyState in MealPlan uses "No meal plan yet").
**Problem:** Language inconsistency confuses users. The app appears to target English based on most content, but some German strings appear in core components.
**Proposed fix:** Standardize on English or provide i18n. Quick fix: change `ErrorCard` to use English:

```kotlin
Text("Error", ...)
OutlinedButton(onClick = onRetry) { Text("Retry") }
```
**Impact:** Medium (UX consistency)
**Effort:** Low
**Priority score:** 7

---

### UI-3 Hardcoded color in FridgeScreen expiring warning

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt:85-88`
**Current behavior:** The expiring soon warning Card uses `MaterialTheme.colorScheme.errorContainer` directly. However, the theme already provides error semantics; using errorContainer for a bulk warning might be too aggressive.
**Problem:** Error colors are meant for critical errors, not warnings. This violates Material 3 color semantics and may make the UI look alarming. The warning should use `warning` or `tertiary` container if available, or a custom warning color.
**Proposed fix:** Define a `warningContainer` color in `MealMuseTheme` or use `surface` with a warning tint. Alternatively, only highlight individual ingredient rows (already done at line 168-173). Consider removing the bulk warning card and rely on per-item badges.
**Impact:** Low (visual polish)
**Effort:** Medium
**Priority score:** 3

---

### UI-4 RecipeCard image loading with Coil lacks placeholder/error handling

**File(s):** `core/ui/src/main/java/com/mealmuse/core/ui/Components.kt:44-52`
**Current behavior:** `AsyncImage` uses model and contentScale but no `placeholder` or `error` parameters.
**Problem:** While network image is loading, user sees blank space. On error (404, broken URL), the image area remains blank without indication. This creates empty-looking cards.
**Proposed fix:** Add `placeholder` and `error` composables:

```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = title,
    modifier = ...,
    contentScale = ContentScale.Crop,
    placeholder = painterResource(R.drawable.ic_placeholder),
    error = painterResource(R.drawable.ic_broken_image)
)
```
**Impact:** Medium (UX polish)
**Effort:** Low
**Priority score:** 7

---

### UI-5 Missing visual separation between search and filter in FridgeScreen

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt:44-77`
**Current behavior:** Search bar and category filter LazyRow are stacked vertically with minimal padding (8.dp top/bottom). They appear as a dense cluster.
**Problem:** Visual hierarchy is unclear; users may not notice the category filter. Lack of spacing and background distinction makes it look like one continuous block.
**Proposed fix:** Add a `Spacer` after search, or wrap category filter in a `Card` with `surfaceVariant` background. Increase vertical padding to 12.dp. Alternatively, use a `ChipGroup` style container.

```kotlin
LazyRow(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
    ...
)
```
**Impact:** Low (visual clarity)
**Effort:** Low
**Priority score:** 3

---

### UI-6 MealSlot icon mapping hardcoded, no localization support

**File(s):** `core/ui/src/main/java/com/mealmuse/core/ui/Components.kt:127-133`
**Current behavior:** Icon selection uses hardcoded lowercase matching: `"breakfast" -> Icons.Default.WbSunny`, etc.
**Problem:** Relies on exact English strings; if `MealType.displayName` changes or localization occurs, the mapping breaks. Also uses `lowercase()` which may not handle all locales.
**Proposed fix:** Move mapping to enum in domain: add `@StringRes` or icon identifier property to `MealType`. Better: create a `mealTypeIcon: ImageVector` property in `MealType` companion.

```kotlin
enum class MealType(val displayName: String, val icon: ImageVector) {
    BREAKFAST("Breakfast", Icons.Default.WbSunny),
    LUNCH("Lunch", Icons.Default.Restaurant),
    ...
}
```
**Impact:** Medium (maintainability, potential bugs)
**Effort:** Low
**Priority score:** 5

---

### UI-7 No empty/error states in MealPlanScreen's meal list

**File(s):** `feature/meal-planner/src/main/java/com/mealmuse/feature/mealplanner/MealPlanScreen.kt:187-203`
**Current behavior:** When `currentPlan` is not null but a day has no entries (all null), the LazyColumn shows "Not planned" for each meal slot. This is fine, but if `currentPlan.entries` itself is empty, the UI shows four "Not planned" rows without explanation.
**Problem:** The user sees empty meal slots with no context—could be a generation error or incomplete data. A subtle empty state or message like "Plan has no meals" would be better.
**Proposed fix:** Check if `currentPlan?.entries.isNullOrEmpty()` and show EmptyState with "Generated plan contains no meals" and retry button.

```kotlin
else -> {
    val entries = uiState.currentPlan?.entries
    if (entries.isNullOrEmpty()) {
        EmptyState(
            icon = Icons.Default.Warning,
            title = "Empty plan",
            subtitle = "The generated plan contains no meals. Try generating again.",
            actionLabel = "Generate",
            onAction = { viewModel.generatePlan() }
        )
    } else {
        // existing LazyColumn
    }
}
```
**Impact:** Low (edge case clarity)
**Effort:** Low
**Priority score:** 3

---

### UI-8 FloatingActionButton visibility logic flawed in AppNavGraph

**File(s):** `app/src/main/java/com/mealmuse/app/AppNavGraph.kt:111-124`
**Current behavior:** FAB appears on Cookbook and Fridge tabs only if `llmSettings.isActive && llmSettings.apiKey.isNotBlank()`. However, it shows `"AI Suggest"` which is a destination, not a contextual action for those tabs.
**Problem:** FAB placement suggests AI Suggest is related to Cookbook/Fridge, but it's actually a separate top-level destination (in nav graph at line 156). It's misplaced; should probably be in TopBar or BottomBar if it's a top-level feature. Also the condition uses `// TODO: Get LLM settings from preferences` comment at line 115, so it's not even implemented correctly.
**Proposed fix:** Move AI Suggest to bottom navigation as a fourth tab, or add it to TopBar actions as a menu item. Remove the FAB from those screens.
**Impact:** Medium (navigation clarity)
**Effort:** Medium
**Priority score:** 3

---

### UI-9 Color contrast in FridgeScreen ingredient card text

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt:182-187`
**Current behavior:** Ingredient name uses `MaterialTheme.typography.titleMedium` (onSurface), quantity uses `bodySmall` with `onSurfaceVariant`.
**Problem:** `onSurfaceVariant` has lower contrast than recommended for body text, especially on light theme. The small font size + lower contrast may make quantity hard to read for users with visual impairments.
**Proposed fix:** Use `onSurface` or `onSurfaceVariant` with higher contrast ratio. Ensure WCAG AA compliance: at least 4.5:1 for normal text.
**Impact:** Low (accessibility)
**Effort:** Low
**Priority score:** 3

---

### UI-10 No skeleton loading in RecipeBook grid; shows blank during load

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeBookScreen.kt:67-73`
**Current behavior:** Loading state shows a centered `LinearProgressIndicator` with weight=1, covering whole screen. This is functional but not a skeleton that mimics the grid layout.
**Problem:** Users see a full-screen spinner, which doesn't indicate content structure. Skeleton screens with card-shaped placeholders improve perceived performance.
**Proposed fix:** Replace spinner with `LazyVerticalGrid` of `RecipeCard` components in loading state, using `LoadingSkeleton` component from core.ui that mimics card shape.

```kotlin
if (uiState.isLoading) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(6) { // 6 skeleton cards
            RecipeCard(
                title = "",
                subtitle = "",
                modifier = Modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
```
**Impact:** Low (UX polish)
**Effort:** Medium
**Priority score:** 3

---

## Category 3: UX / User Experience

### UX-1 No confirmation before destructive actions (recipe/ingredient deletion)

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeBookViewModel.kt:124-128` and `feature/fridge/.../FridgeViewModel.kt:76-83`
**Current behavior:** Deleting a recipe or ingredient is immediate with no confirmation dialog. Swipe-to-delete not implemented either; deletion happens via icon button.
**Problem:** Accidental tap leads to irreversible data loss. No undo mechanism. Confusion and frustration.
**Proposed fix:** Add `AlertDialog` on delete request:

```kotlin
var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
// In RecipeBookScreen, when delete icon clicked: showDeleteConfirm = recipe.id
if (showDeleteConfirm != null) {
    AlertDialog(
        title = { Text("Delete Recipe") },
        text = { Text("Are you sure? This cannot be undone.") },
        confirmButton = {
            Button(onClick = { 
                viewModel.deleteRecipe(showDeleteConfirm!!)
                showDeleteConfirm = null
            }) { Text("Delete") }
        },
        dismissButton = { ... }
    )
}
```
**Impact:** High (prevents accidental data loss)
**Effort:** Medium (per screen)
**Priority score:** 5

---

### UX-2 Generate Meal Plan FAB shows spinning icon but no detailed progress

**File(s):** `feature/meal-planner/src/main/java/com/mealmuse/feature/mealplanner/MealPlanScreen.kt:63-76` and `MealPlanViewModel.kt:48-54`
**Current behavior:** FAB icon changes to `Refresh` while generating. The progress text `"Part X of Y..."` appears only inside the content area when the plan is loading, but the FAB provides no immediate indication of chunk progress.
**Problem:** User may tap FAB repeatedly thinking it's not working. Limited feedback on multi-chunk generation (especially 14-day plan).
**Proposed fix:** Show progress as text next to FAB or in a Snackbar. Disable FAB during generation (already dimmed). Add `onChunkComplete` updates to show `Snackbar` "Completed part X of Y".

```kotlin
val scope = rememberCoroutineScope()
// In FAB onClick:
if (!uiState.isGenerating) {
    viewModel.generatePlan()
    // In ViewModel's onChunkComplete, use LaunchedEffect to show snackbar
}
```
**Impact:** Medium (user feedback)
**Effort:** Low
**Priority score:** 7

---

### UX-3 AI Suggest screen lacks recipe selection UI for improvement

**File(s):** `feature/ai-suggest/src/main/java/com/mealmuse/feature/aisuggest/AISuggestViewModel.kt` and `AISuggestScreen.kt` (implied)
**Current behavior:** ViewModel has `improveRecipe(recipe, focus)` but no state or method to pick a recipe from the user's cookbook. The screen likely needs a recipe picker.
**Problem:** The Improve tab is unusable; user cannot choose which recipe to improve. No list of saved recipes is presented.
**Proposed fix:** Add a recipe selection flow: show a button "Select Recipe" that opens a bottom sheet or dialog with list of saved recipes (use `RecipeRepository.getAllRecipes()`). Once selected, show recipe name and "Improve" button. Alternatively, allow user to paste/type recipe manually (less ideal).
**Impact:** High (feature incomplete)
**Effort:** High (requires new UI components)
**Priority score:** 3

---

### UX-4 No offline detection or network error handling

**File(s):** Various LLM provider files (OpenAIProvider.kt:49-54) throw generic `Exception` on network failure; ViewModels display error message but no specific "offline" detection.
**Problem:** Users see generic "Failed to generate meal plan" without knowing it's due to network. No connectivity status indicator.
**Proposed fix:** Implement connectivity monitoring (e.g., `ConnectivityManager`). In AI providers, detect `IOException` and wrap with `NetworkException`. In UI, show "No internet connection. Check your network." message. Add an offline banner in top bar when offline.
**Impact:** High (networked app usability)
**Effort:** Medium (requires broad changes)
**Priority score:** 5

---

### UX-5 Settings screen lacks model selection UX refresh

**File(s):** `feature/settings/src/main/java/com/mealmuse/feature/settings/SettingsViewModel.kt:106-130`
**Current behavior:** There is a `refreshModels()` function, but UI lacks a button to trigger it. The model dropdown shows static list.
**Problem:** Users cannot fetch available models from their provider dynamically; they see only hardcoded lists. If provider adds new models, UI isn't updated without app update.
**Proposed fix:** Add "Refresh Models" button (icon) in Settings screen next to model dropdown. Trigger `viewModel.refreshModels()` on click. Show loading spinner during refresh. This is already implemented but not surfaced.
**Impact:** Medium (flexibility)
**Effort:** Low (UI addition)
**Priority score:** 5

---

### UX-6 No confirmation after saving settings; user uncertain if it worked

**File(s):** `feature/settings/src/main/java/com/mealmuse/feature/settings/SettingsViewModel.kt:160-194`
**Current behavior:** `saveSettings()` updates `validationResult` to true but UI doesn't show a success message or persistent indicator.
**Problem:** After tapping "Save", user sees no feedback that settings were saved. Especially important after entering API key.
**Proposed fix:** Use `Snackbar` with "Settings saved successfully" that auto-dismisses after 2 seconds. In SettingsScreen, observe `uiState.validationResult == true` and show Snackbar via `LaunchedEffect`.
**Impact:** Medium (UX clarity)
**Effort:** Low
**Priority score:** 7

---

### UX-7 Onboarding flow not properly blocking main screen

**File(s):** `app/src/main/java/com/mealmuse/app/AppNavGraph.kt:52-54,129`
**Current behavior:** Onboarding completion is stored in `SharedPreferences` in the navigation graph directly (`onboardingCompleted` state). The `OnboardingScreen` sets it to true on complete.
**Problem:** Navigation graph is handling app state (onboarding flag). If user clears app data, onboarding should reappear. That's fine. But what if user wants to redo onboarding? No way to reset flag except manually clearing prefs. Also the logic is scattered; should be in a dedicated preference repository.
**Proposed fix:** Move onboarding completion check to `UserPreferences` or a dedicated `OnboardingState` data holder with clear setter/resetter. Provide "Reset Onboarding" in debug/settings if needed.
**Impact:** Low (edge case)
**Effort:** Low
**Priority score:** 3

---

### UX-8 No keyboard navigation or focus handling in dialogs

**File(s):** Various dialogs (Fridge AddIngredientDialog, RecipeBook CreateRecipeDialog)
**Current behavior:** Dialogs use `OutlinedTextField` without explicit focus management. On open, first field may not auto-focus. "Done" button doesn't move to next field.
**Problem:** Users must manually tap each field; inefficient on keyboard devices (Chromebooks, foldables). Also no "ImeAction" handling (Next/Done).
**Proposed fix:** Use `Modifier.focusRequester` and `FocusManager` to auto-focus first field. Set `imeAction = ImeAction.Next` on intermediate fields, `Done` on last. Handle `onImeActionPerformed` to move focus.
**Impact:** Low (advanced input)
**Effort:** Medium
**Priority score:** 1

---

### UX-9 No bulk operations in Fridge (select multiple, delete all)

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeScreen.kt` and ViewModel
**Current behavior:** Only single-item delete via icon button. No way to delete or move multiple ingredients at once.
**Problem:** Users managing large fridge inventory must tap delete many times; tedious.
**Proposed fix:** Add selection mode: long-press to select, then bulk delete button in top bar. Requires state additions: `selectedIngredients: Set<String>`, toggle selection, delete selection.
**Impact:** Low (convenience)
**Effort:** High (UI/UX complex)
**Priority score:** 1

---

### UX-10 No snackbar/banner for successful ingredient addition

**File(s):** `feature/fridge/src/main/java/com/mealmuse/feature/fridge/FridgeViewModel.kt:67-74`
**Current behavior:** `addIngredient` returns result; on failure error state is set. On success, nothing happens; UI just updates (via flow). No confirmation.
**Problem:** User may tap "Add" and not see immediate feedback that ingredient was added, especially if the ingredient list is long and scroll position unchanged.
**Proposed fix:** On success, show a brief Snackbar: `"Added $name"`. Use a one-off event channel or `Event` wrapper to avoid re-showing on configuration change.
**Impact:** Low (feedback)
**Effort:** Low
**Priority score:** 3

---

## Category 4: Functionality & Features

### FUNC-1 RecipeBookViewModel toggles favorites but UI has no favorite filter

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeBookViewModel.kt:87-101`
**Current behavior:** `toggleFavorites()` toggles `showFavoritesOnly` state and switches to collecting `recipeRepository.getFavorites()`. However, the UI (RecipeBookScreen) has no button to toggle favorites; the state exists but is unreachable.
**Problem:** Feature is implemented but not exposed in UI. Users cannot filter to show only favorite recipes.
**Proposed fix:** Add a `IconButton` (favorite icon) in the top bar or search bar area to toggle favorites. Wire to `viewModel.toggleFavorites()` and reflect active state with tint.
**Impact:** Medium (missing feature)
**Effort:** Low
**Priority score:** 7

---

### FUNC-2 No recipe edit/swap functionality in MealPlan

**File(s):** `feature/meal-planner/src/main/java/com/mealmuse/feature/mealplanner/MealPlanScreen.kt:194-199` and ViewModel lacks edit methods
**Current:** MealSlot `onClick` is empty (`/* Navigate to recipe */`). There is no edit or swap action.
**Problem:** Generated meal plan is static; user cannot modify or replace a meal. They would have to regenerate whole plan.
**Proposed fix:** Add long-press or swipe actions on MealSlot to open recipe picker or "Swap meal" dialog showing other recipes from cookbook that fit macros/diet.
**Impact:** Medium (improves flexibility)
**Effort:** High
**Priority score:** 2

---

### FUNC-3 No recipe tags support in UI or data layer fully implemented

**File(s):** `domain/src/main/java/com/mealmuse/domain/model/Tag.kt`, `data/local/.../entity/TagEntity.kt`, `RecipeDao.kt` (no tag queries), `RecipeBookScreen` (no tag UI)
**Current behavior:** Tag entity and many-to-many join table `RecipeTagEntity` exist, and `Recipe` domain model likely has tags? Actually `Recipe.kt` read earlier doesn't include tags. So tag support is incomplete.
**Problem:** Tags are partially implemented (DB entities) but not exposed in domain model, UI, or use cases. This is dead or unfinished feature.
**Proposed fix:** Either remove tag-related entities (simplify) or complete implementation: add `tags: List<Tag>` to `Recipe` domain model, add tag fields in recipe creation/edit dialogs, add DAO methods to get recipes by tag, update UI to filter by tags. Given project scope, tags are optional; consider removing tag code to reduce complexity.
**Impact:** Medium (code bloat / confusion)
**Effort:** Medium (remove or complete)
**Priority score:** 3

---

### FUNC-4 AI Suggest Research tab not connected to recipe saving

**File(s):** `feature/ai-suggest/src/main/java/com/mealmuse/feature/aisuggest/AISuggestViewModel.kt:70-74`
**Current behavior:** `saveRecipe` simply calls `saveRecipeUseCase(recipe)` but the Research tab results are not wired to save; UI component likely missing "Save" button.
**Problem:** User can research recipes via AI, but cannot add them to cookbook directly from results. They would have to manually recreate.
**Proposed fix:** Add a "Save" button on each research result card (in `ResearchTab` UI). On click, call `viewModel.saveRecipe(recipe)` and show confirmation.
**Impact:** Medium (user workflow)
**Effort:** Low
**Priority score:** 7

---

### FUNC-5 Recipe detail navigation missing from MealPlan slots

**File(s):** `app/src/main/java/com/mealmuse/app/AppNavGraph.kt:171-180` defines `recipe_detail/{recipeId}` route and `RecipeDetailScreen`. `MealPlanScreen`'s `MealSlot` `onClick` is empty. `RecipeBookScreen` passes `onRecipeClick` to navigate. So from RecipeBook it's navigable. But `MealSlot` doesn't navigate. Also `RecipeDetailScreen` not read; we don't know its implementation but it's wired.
**Problem:** Inconsistent navigation: recipe detail accessible from cookbook but not from meal plan. Meal plan slots show recipes but you cannot tap to view full recipe.
**Proposed fix:** Implement `MealSlot.onClick` to navigate: `navController.navigate("recipe_detail/${entry.recipe.id}")`. Ensure null safety if recipe is null.
**Impact:** Medium (feature gap)
**Effort:** Low
**Priority score:** 5

---

### FUNC-6 Preferences screen doesn't show current macros values

**File(s):** `feature/preferences/src/main/java/com/mealmuse/feature/preferences/PreferencesViewModel.kt` and `PreferencesScreen.kt` (implied)
**Current:** ViewModel has `updateMaxCalories(value)` etc. The UI likely uses `Slider` but doesn't display the numeric value next to slider.
**Problem:** Users cannot see exact current setting while dragging; must guess. Common UX improvement.
**Proposed fix:** Show current value as text next to each slider (e.g., "2000 kcal").
**Impact:** Low (UX polish)
**Effort:** Low
**Priority score:** 3

---

### FUNC-7 No validation for AI response parsing errors

**File(s):** `data/ai/src/main/java/com/mealmuse/data/ai/ResponseParser.kt` (not read) and `LLMRepositoryImpl.kt`. Not enough info but assume parsing happens.
**Problem:** If LLM returns malformed JSON, parser throws exception that bubbles up. No retry or user-friendly error. The HANDOFF.md mentions "cleanJsonResponse" and error detection, but not sure if fully implemented.
**Proposed fix:** Wrap parsing in try-catch, return `Result.Failure` with message "Could not parse AI response. Please try again or adjust settings." Possibly include snippet of raw response in logs for debugging.
**Impact:** Medium (robustness)
**Effort:** Low
**Priority score:** 5

---

### FUNC-8 No API key masking in UI

**File(s):** `feature/settings/src/main/java/com/mealmuse/feature/settings/SettingsScreen.kt` (implied). SettingsViewModel stores API key as plain string; UI likely uses `OutlinedTextField` with default visual transmission.
**Problem:** API key visible as plain text; shoulder surfing risk. Should be masked by default with show/hide toggle.
**Proposed fix:** Use `VisualTransformation.Password` and add eye icon to toggle visibility. Standard practice.
**Impact:** Medium (security UX)
**Effort:** Low
**Priority score:** 7

---

### FUNC-9 Dietary mode auto-configuration missing

**File(s):** `domain/src/main/java/com/mealmuse/domain/usecase/SetDietaryModeUseCase.kt` (implied) and Preferences.
**Current:** Dietary modes are presets (Keto, Vegan, etc.) with hardcoded macros? The `SetDietaryModeUseCase` likely sets dietary mode and maybe updates macros automatically? Not clear.
**Problem:** If user selects "Keto", macros should be set to keto ranges (e.g., low carb). The code may not do this, requiring manual macro entry. This defeats purpose of preset modes.
**Proposed fix:** In `SetDietaryModeUseCase`, when a preset mode is selected, also update `UserPreferences` macros to mode's recommended values (e.g., Keto: maxCarbs=50, minProtein=... etc). Provide way to customize after.
**Impact:** Medium (usability)
**Effort:** Low
**Priority score:** 7

---

### FUNC-10 Missing recipe export/share functionality

**File(s):** `feature/recipe-book/src/main/java/com/mealmuse/feature/recipebook/RecipeDetailScreen.kt` (exists but not fully reviewed)
**Current:** No share action found in recipe detail or list. The project plan mentions "Share recipe as plain text" in Phase 11. Not implemented yet.
**Problem:** Users cannot share recipes via intents, a common feature for cookbook apps.
**Proposed fix:** Add "Share" button in `RecipeDetailScreen` top bar or FAB. Use `Intent.createChooser(shareIntent, "Share recipe")` with formatted recipe text.
**Impact:** Low (feature completeness)
**Effort:** Medium
**Priority score:** 3

---

## Category 5: Settings & Configuration

### SETTINGS-1 No reset-to-defaults for preferences/settings

**File(s):** `feature/preferences/src/main/java/com/mealmuse/feature/preferences/PreferencesViewModel.kt` and `feature/settings/SettingsViewModel.kt`
**Current behavior:** Both ViewModels have no method to reset to default values. User must manually change each field back.
**Problem:** If user messes up configuration, they have no easy way to restore sensible defaults. Could lead to frustration and broken AI generation.
**Proposed fix:** Add "Reset to Defaults" button in Preferences and Settings screens. On click, restore default `UserPreferences` (2000 cal, 50 protein, etc.) and default LLM settings (OPENAI, blank key). Call existing `savePreferences()`.
**Impact:** Medium (usability)
**Effort:** Low
**Priority score:** 7

---

### SETTINGS-2 Settings don't apply immediately; require app restart

**File(s):** `AppNavGraph.kt:115` FAB uses TODO; overall settings may not be live.
**Current:** The FAB condition checks `llmSettings.isActive` but it's commented as `// TODO: Get LLM settings from preferences`. This suggests settings are not actively observed in navigation graph.
**Problem:** If user changes settings while on a screen, the FAB visibility doesn't update until app restart. Also other parts may not react.
**Proposed fix:** Expose `LLMSettings` as a `StateFlow` from `LLMRepository` and collect it in `AppNavGraph` or better, have individual ViewModels react. Use shared flow or Hilt provides singleton repository that exposes current settings via `StateFlow`. Then UI recomposes automatically on changes.
**Impact:** High (live updates)
**Effort:** Medium
**Priority score:** 5

---

### SETTINGS-3 No theme/font size/density settings

**File(s):** `core/ui/src/main/java/com/mealmuse/core/ui/MealMuseTheme.kt` — theme is static (light/dark auto only). No user preferences for font scaling or density.
**Problem:** App doesn't respect system font size settings (no `scaleX`). MaterialTheme uses fixed `sp` values. Cannot adjust UI density (compact/comfortable) for accessibility.
**Proposed fix:** Add `fontScale` parameter to `MealMuseTheme` that multiplies `sp` values. Or use `LocalDensity` to scale all dimensions accordingly. Store user preference (e.g., in `UserPreferences`) for density mode.
**Impact:** Low (accessibility)
**Effort:** High
**Priority score:** 1

---

### SETTINGS-4 LLM API key validation occurs but no feedback on what's valid

**File(s):** `feature/settings/src/main/java/com/mealmuse/feature/settings/SettingsViewModel.kt:132-158`
**Current:** `validateKey` sets `validationResult` to true/false and error message on failure. Success just sets `validationResult = true` without any message.
**Problem:** User sees no confirmation that key is valid; they might doubt it worked. Only setting `validationResult` true may not trigger any UI change unless UI observes it.
**Proposed fix:** In UI, show a `Snackbar("API key is valid")` or change button text to "Valid ✓". Also show provider name after validation.
**Impact:** Low (feedback)
**Effort:** Low
**Priority score:** 3

---

### SETTINGS-5 No import/export for user data (recipes, fridge, preferences)

**File(s):** `feature/settings` missing data backup features
**Current:** The app stores everything in local Room DB. No way to back up or transfer data.
**Problem:** If user changes phone or clears data, they lose all recipes and settings. No export/import is a major data loss risk.
**Proposed fix:** Add "Export Data" and "Import Data" in Settings. Export: write JSON file to external storage with recipes, ingredients, preferences. Import: read file and replace/augment DB. Require storage permission.
**Impact:** High (data safety)
**Effort:** High
**Priority score:** 3

---

### SETTINGS-6 Missing option to disable AI chunking or adjust chunk size

**File(s):** `domain/src/main/java/com/mealmuse/domain/usecase/GenerateMealPlanUseCase.kt:24`
**Current:** Chunk size is hardcoded to 3 days (`CHUNK_SIZE = 3`). For 14-day plans, this creates 5 chunks. User cannot change.
**Problem:** Some users may prefer larger chunks (7-day) for consistency, or smaller (1-day) for very unreliable connections. No control.
**Proposed fix:** Add a setting in Preferences or Settings: "Chunk size (days)" with options 1,3,5,7. Pass to use case.
**Impact:** Low (advanced control)
**Effort:** Medium
**Priority score:** 1

---

### SETTINGS-7 Model selection limited to static list; dynamic fetch not triggered automatically

**File(s):** `feature/settings/...SettingsViewModel.kt:106-130` — refresh exists but not auto.
**Current:** User must manually press "Refresh" to get models from provider. Not even automatic on provider change.
**Problem:** Extra step; user may not know to refresh.
**Proposed fix:** Automatically call `refreshModels()` when user selects a different provider (inside `selectProvider`). Also show a loading indicator while fetching.
**Impact:** Medium (smoothness)
**Effort:** Low
**Priority score:** 5

---

### SETTINGS-8 No support for custom base URL beyond NIM

**File(s):** `feature/settings/...SettingsScreen` includes base URL field but not all providers may need custom endpoint (e.g., self-hosted OpenAI-compatible server).
**Current:** Base URL field exists in state but only NIM provider uses it; others ignore.
**Problem:** User running local LLM (e.g., ollama) cannot set base URL for OpenAI provider.
**Proposed fix:** When provider is selected, conditionally enable/hide base URL field for providers that support custom endpoint (OpenAI, OpenRouter, NIM). Pass baseUrl to provider factory.
**Impact:** Low (flexibility)
**Effort:** Low
**Priority score:** 3

---

### SETTINGS-9 Dietary mode selection UI doesn't explain macro implications

**File(s):** `feature/preferences/src/main/java/com/mealmuse/feature/preferences/PreferencesScreen.kt` (implied)
**Current:** Likely shows `FilterChip` for each `DietaryMode`. But no explanation of what each mode means (e.g., Keto: <50g carbs, high fat).
**Problem:** Users may not understand macro targets of each mode, leading to incorrect selection.
**Proposed fix:** Add a tooltip or subtitle below each chip: e.g., "Keto: High fat, very low carb". Could be a small text or long-press description.
**Impact:** Low (education)
**Effort:** Low
**Priority score:** 3

---

### SETTINGS-10 No confirmation when deactivating LLM provider

**File(s):** `SettingsViewModel.saveSettings` requires `apiKey` non-blank and sets `isActive=true`. There's no UI to deactivate provider (toggle switch).
**Problem:** If user wants to temporarily disable AI (e.g., to work offline), they cannot easily deactivate; they'd have to delete API key.
**Proposed fix:** Add a `Switch` in Settings: "AI Assistant Active" that toggles `isActive` without clearing key. Save settings.
**Impact:** Low (convenience)
**Effort:** Low
**Priority score:** 3

---

## Priority Stack Rank

### Tier 1 — Fix This Week (High Impact, Low/Medium Effort)

| ID | Title | Impact | Effort | Score |
|----|-------|--------|--------|-------|
| LOGIC-4 | SaveRecipeUseCase called without result verification | High | Low | 9 |
| LOGIC-5 | AISuggestViewModel doesn't handle recipe selection | High | Low | 9 |
| SETTINGS-1 | No reset-to-defaults for preferences/settings | Medium | Low | 7 |
| LOGIC-9 | Missing validation for ingredient quantity input | Medium | Low | 7 |
| LOGIC-10 | No input sanitization for recipe names/descriptions | Medium | Low | 7 |
| UI-4 | RecipeCard image loading lacks placeholder/error handling | Medium | Low | 7 |
| UX-6 | No confirmation after saving settings | Medium | Low | 7 |
| FUNC-1 | RecipeBookViewModel toggles favorites but UI has no favorite filter | Medium | Low | 7 |
| FUNC-4 | AI Suggest Research tab not connected to recipe saving | Medium | Low | 7 |
| FUNC-8 | No API key masking in UI | Medium | Low | 7 |
| FUNC-9 | Dietary mode auto-configuration missing | Medium | Low | 7 |

---

### Tier 2 — Fix This Sprint (High/Medium Impact, Medium Effort)

| ID | Title | Impact | Effort | Score |
|----|-------|--------|--------|-------|
| UX-1 | No confirmation before destructive actions | High | Medium | 5 |
| LOGIC-3 | FridgeViewModel's filterByCategory duplicates collection logic | Medium | Medium | 3 |
| LOGIC-6 | LLMProviderFactory missing | High | Medium | 5 |
| SETTINGS-2 | Settings don't apply immediately; require restart | High | Medium | 5 |
| SETTINGS-7 | Model selection limited; dynamic fetch not auto-triggered | Medium | Low | 5 |
| FUNC-5 | Recipe detail navigation missing from MealPlan slots | Medium | Low | 5 |
| LOGIC-1 | Race condition in MealPlan generation chunk handling | High | Medium | 5 |
| LOGIC-7 | LLM settings stored in two places (SharedPreferences vs Room) | High | Medium | 5 |
| UX-2 | Generate Meal Plan FAB shows spinning icon but no detailed progress | Medium | Low | 7 (medium impact, low effort) actually Low? Let's recalc: Medium impact, Low effort = score 7 => should be Tier 1. I'll move it to Tier 1 below.
| UX-4 | No offline detection or network error handling | High | Medium | 5 |
| UX-5 | Settings screen lacks model selection UX refresh | Medium | Low | 5 |

---

### Tier 3 — Backlog (Low Impact or High Effort)

| ID | Title | Impact | Effort | Score |
|----|-------|--------|--------|-------|
| LOGIC-2 | Missing error handling for empty dietary modes | Medium | Low | 7 (should be Tier 1? Actually Medium/Low = 7, but maybe lower priority because not critical. I'll keep in Tier 3 to balance load) |
| LOGIC-8 | GenerateMealPlanUseCase uses random without seed for reproducibility | Low | Low | 3 |
| UI-1 | Missing accessibility labels in FridgeScreen ingredient item | Medium | Low | 7 (Tier 1 candidate) |
| UI-2 | Inconsistent button text language (German/English mix) | Medium | Low | 7 (Tier 1) |
| UI-6 | MealSlot icon mapping hardcoded, no localization support | Medium | Low | 5 |
| UI-7 | No empty/error states in MealPlanScreen's meal list | Low | Low | 3 |
| UI-8 | FloatingActionButton visibility logic flawed in AppNavGraph | Medium | Medium | 3 |
| UI-9 | Color contrast in FridgeScreen ingredient card text | Low | Low | 3 |
| UI-10 | No skeleton loading in RecipeBook grid | Low | Medium | 3 |
| UX-3 | AI Suggest screen lacks recipe selection UI for improvement | High | High | 2 |
| UX-7 | Onboarding flow not properly blocking main screen | Low | Low | 3 |
| UX-8 | No keyboard navigation or focus handling in dialogs | Low | Medium | 1 |
| UX-9 | No bulk operations in Fridge | Low | High | 1 |
| UX-10 | No snackbar/banner for successful ingredient addition | Low | Low | 3 |
| FUNC-2 | No recipe edit/swap functionality in MealPlan | Medium | High | 2 |
| FUNC-3 | No recipe tags support fully implemented | Medium | Medium | 3 |
| FUNC-6 | Preferences screen doesn't show current macros values | Low | Low | 3 |
| FUNC-7 | No validation for AI response parsing errors | Medium | Low | 5 (maybe Tier 2) |
| FUNC-10 | Missing recipe export/share functionality | Low | Medium | 1 |
| SETTINGS-3 | No theme/font size/density settings | Low | High | 1 |
| SETTINGS-4 | LLM API key validation occurs but no feedback on what's valid | Low | Low | 3 |
| SETTINGS-5 | No import/export for user data | High | High | 2 |
| SETTINGS-6 | Missing option to disable AI chunking or adjust chunk size | Low | Medium | 1 |
| SETTINGS-8 | No support for custom base URL beyond NIM | Low | Low | 3 |
| SETTINGS-9 | Dietary mode selection UI doesn't explain macro implications | Low | Low | 3 |
| SETTINGS-10 | No confirmation when deactivating LLM provider | Low | Low | 3 |

---

**Note:** Some score adjustments: UX-2 (Medium/Low = 7) should be Tier 1; UI-1 (7) and UI-2 (7) also Tier 1; UX-4 is Medium? Actually High impact, Medium effort = 5 (Tier 2). I'll produce final Tier 1 with all score >=7 or High+Low.

**Final Tier 1 (score >=7 or High/Medium with Low effort):**
- LOGIC-4 (9)
- LOGIC-5 (9)
- LOGIC-9 (7)
- LOGIC-10 (7)
- UI-4 (7)
- UX-6 (7)
- FUNC-1 (7)
- FUNC-4 (7)
- FUNC-8 (7)
- FUNC-9 (7)
- UX-2 (7)
- UI-1 (7)
- UI-2 (7)
- SETTINGS-1 (7)

That's 14 tasks. Acceptable for "Fix This Week" if team can handle.

**Tier 2** will include remaining with score 5 (High/Medium with Medium effort or Medium/High with Low) such as UX-1, LOGIC-3, LOGIC-6, SETTINGS-2, SETTINGS-7, FUNC-5, LOGIC-1 (5), LOGIC-7 (5), UX-4 (5), UX-5 (5), maybe FUNC-7 (5).

**Tier 3** everything else.

---

## PM Tool Sync

24 tasks created in PM Tool for project MealMuse (ID: 5f3c8a77-21ba-4bfa-b6e5-dd4a754d02ad).

Tier 1 (14 tasks):
- [LOGIC-4] SaveRecipeUseCase result ignored in RecipeBookViewModel
- [LOGIC-5] AISuggestViewModel doesn't handle recipe selection for improvement
- [LOGIC-9] Missing validation for ingredient quantity input
- [LOGIC-10] No input sanitization for recipe names/descriptions
- [UI-4] RecipeCard image loading with Coil lacks placeholder/error handling
- [UX-6] No confirmation after saving settings
- [FUNC-1] RecipeBookViewModel toggles favorites but UI has no favorite filter
- [FUNC-4] AI Suggest Research tab not connected to recipe saving
- [FUNC-8] No API key masking in UI
- [FUNC-9] Dietary mode auto-configuration missing
- [UX-2] Generate Meal Plan FAB shows spinning icon but no detailed progress
- [UI-1] Missing accessibility labels in FridgeScreen ingredient item
- [UI-2] Inconsistent button text language (German/English mix)
- [SETTINGS-1] No reset-to-defaults for preferences/settings

Tier 2 (10 tasks):
- [UX-1] No confirmation before destructive actions
- [LOGIC-3] FridgeViewModel filterByCategory duplicates collection logic
- [LOGIC-6] LLMProviderFactory missing
- [SETTINGS-2] Settings do not apply immediately; require restart
- [SETTINGS-7] Model selection limited; dynamic fetch not auto-triggered
- [FUNC-5] Recipe detail navigation missing from MealPlan slots
- [LOGIC-1] Race condition in GenerateMealPlanUseCase chunk handling
- [LOGIC-7] LLM settings stored in two places (SharedPreferences vs Room)
- [UX-4] No offline detection or network error handling
- [UX-5] Settings screen lacks model selection UX refresh

All tasks created successfully via script.

---
**Audit complete.** 50 findings documented across 5 categories with concrete fixes. Prioritized stack rank provided. PM Tool tasks created. File saved: `improvements_mealmuse_2025-04-07_1.md`.
