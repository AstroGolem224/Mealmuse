# Cookbook Fix Implementation Plan

Dieses Dokument beschreibt den 9-Phasen-Plan zur Behebung der Abstürze und Fehler im Cookbook-Modul der MealMuse-App.

## Phase 1: Fehleranalyse (Analysis & Triage)
- [x] Analyse der Absturzursache: Das `LazyVerticalGrid` in `RecipeBookScreen.kt` befindet sich in einer vertikalen `Column` ohne Höhenbeschränkung. Dies führt in Jetpack Compose unweigerlich zu einer `IllegalStateException` ("measured with an infinity maximum height constraints").
- [x] Analyse des ViewModels: Jeder Aufruf von `loadRecipes()` oder `search()` startet eine neue Coroutine, die einen endlosen Room-Datenbank-Flow (`searchRecipeUseCase(query).collect`) einsammelt. Da alte Jobs nicht gecancelt werden, überschreiben sich die States (Race Conditions) und erzeugen Memory Leaks.
- [x] Analyse der Suchleiste: Das `OutlinedTextField` aktualisiert lediglich eine lokale `searchQuery`-Variable, ruft aber niemals `viewModel.search()` auf.

## Phase 2: Fix UI Layout Crash (LazyVerticalGrid)
- [x] In `RecipeBookScreen.kt`: Hinzufügen von `modifier = Modifier.weight(1f)` zum `LazyVerticalGrid`.
- [x] Sicherstellen, dass die `Column` den gesamten Bildschirm (`fillMaxSize()`) füllt, damit die Gewichtsverteilung korrekt berechnet wird.

## Phase 3: Fix ViewModel Flow Memory Leaks (State Management)
- [x] In `RecipeBookViewModel.kt`: Einführen einer `searchJob: Job?` Variable.
- [x] Vor jedem neuen `viewModelScope.launch` in `loadRecipes()`, `search()` und `toggleFavorites()` den alten `searchJob?.cancel()` aufrufen.
- [x] Dies stellt sicher, dass stets nur auf den aktuellsten Datenbank-Flow gehört wird und Änderungen in der DB nicht mehrfach emittiert werden.

## Phase 4: Implementierung der Suchfunktionalität (Search Sync)
- [x] In `RecipeBookScreen.kt`: Den `onValueChange`-Block des Such-`OutlinedTextField` anpassen.
- [x] Bei jeder Eingabe zusätzlich zu `searchQuery = it` auch `viewModel.search(it)` aufrufen.
- [x] Dasselbe für den Clear-Button (`Icons.Default.Clear`): `viewModel.search("")` ausführen.

## Phase 5: UX-Verbesserung beim Löschen von Rezepten
- [x] In `RecipeDetailScreen.kt`: Das `onClick` Event des Delete-Buttons anpassen.
- [x] Zuerst `viewModel.deleteRecipe(it.id)` aufrufen.
- [x] Danach sofort `onNavigateBack()` triggern, damit der Nutzer nicht auf einem leeren Screen ("Recipe not found") strandet.

## Phase 6: Beheben des Empty-State Rendering Bugs
- [x] In `RecipeBookScreen.kt`: Die Logik des `when`-Blocks optimieren, damit das `EmptyState` sauber dargestellt wird und nicht den restlichen Screen verdrängt.
- [x] In `RecipeDetailScreen.kt`: Den Null-Check für `recipe` robuster gestalten.

## Phase 7: Code Cleanup & Refactoring
- [x] Entfernen von ungenutzten Imports in den veränderten Dateien.
- [x] Überprüfen von UI-Alignments und Spacings in den Cards, um sicherzustellen, dass die Textüberläufe (Ellipsis) korrekt funktionieren.

## Phase 8: Tests & Validierung
- [x] App via Bash neu kompilieren (`./gradlew app:assembleDebug`), um Build-Fehler auszuschließen.
- [x] Verifikation, dass keine UI-Komponenten mehr in Endlosschleifen messen (Infinity Constraints).

## Phase 9: Deployment & Handover
- [x] Alle Änderungen committen und ins PM-Tool tracken.
- [x] Abschluss und Handover.