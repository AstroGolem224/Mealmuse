# MealMuse - Architekturübersicht

MealMuse ist eine vollständig lokale, KI-gestützte Android-App zur Essensplanung. Sie nutzt **Jetpack Compose** für das UI, **MVVM** als Präsentationsmuster und folgt den Prinzipien der **Clean Architecture**.

## 1. Schichtenarchitektur (Clean Architecture)

Das Projekt ist in 15 Gradle-Module unterteilt, die strikt getrennte Verantwortlichkeiten haben:

*   **`:app` (Entry Point):** Verbindet alles, enthält die `MainActivity`, den `AppNavGraph` und die Hilt-DI-Konfiguration.
*   **`:feature:*` (Presentation Layer):** Beinhaltet die UI (Compose) und ViewModels. Module: `meal-planner`, `recipe-book`, `fridge`, `ai-suggest`, `settings`, `preferences`, `onboarding`.
*   **`:domain` (Business Logic Layer):** Enthält die reinen Kotlin-Modelle (`Recipe`, `MealPlan`, `Ingredient`), Use Cases (`GenerateMealPlanUseCase`, `SaveRecipeUseCase`) und Repository-Interfaces. Diese Schicht hat **keine** Android-Abhängigkeiten.
*   **`:data:*` (Data Layer):** Implementiert die Repositories.
    *   `:data:local`: Room-Datenbank, DAOs und Entity-Mappers für persistente Speicherung.
    *   `:data:ai`: Multi-LLM-Integration (OpenAI, Anthropic, OpenRouter, NIM), Prompt-Generierung und JSON-Parsing.
    *   `:data:remote`: Web-Scraping und Nutrition-APIs.
*   **`:core:*` (Shared Utilities):**
    *   `:core:ui`: Shared Composables, Theme, Design System (`MealMuseTheme`, `RecipeCard`, etc.).
    *   `:core:common`: Utility-Klassen, Extensions, `Result`-Klassen für Error-Handling.

## 2. Datenfluss (Unidirectional Data Flow)

Die App nutzt Unidirectional Data Flow (UDF) kombiniert mit StateFlows:
1.  **UI (Compose)** sendet User Intents an das **ViewModel** (z.B. `viewModel.generatePlan()`).
2.  Das **ViewModel** ruft einen **Use Case** im Domain Layer auf.
3.  Der **Use Case** orchestriert Repositories (z.B. holt Zutaten aus dem `FridgeRepository` und ruft das `LLMRepository` auf).
4.  Das **Repository** kommuniziert mit der Datenquelle (Room DB oder REST API / LLM).
5.  Das Ergebnis wird als `Result<T>` an das ViewModel zurückgegeben.
6.  Das ViewModel aktualisiert den `StateFlow<UiState>`.
7.  Das **UI (Compose)** reagiert auf den neuen State und rekomponiert.

## 3. KI-Integration (Multi-LLM)

Die App kommuniziert direkt mit verschiedenen LLM-Providern über REST/HTTP (via OkHttp).
*   **Provider:** Unterstützt OpenAI, Anthropic, OpenRouter und NVIDIA NIM.
*   **Ablauf:** Die `PromptFactory` generiert den Prompt (inkl. Zutaten und Makro-Zielen) -> der konfigurierte `LLMProvider` sendet den Request -> der `ResponseParser` wandelt die strukturierte JSON-Antwort in Domain-Objekte (`MealPlan`, `Recipe`) um.
*   **Sicherheit:** API-Keys werden lokal in den `SharedPreferences` gespeichert.

## 4. Persistenz (Local-First)

Die gesamte Datenhaltung (außer der KI-Generierung) ist vollständig offline-fähig.
*   Verwendet **Room Database** mit primären Entitäten (`RecipeEntity`, `IngredientEntity`, `MealPlanEntity`, etc.).
*   Beziehungen (1:n, m:n) werden in der Datenbank über Foreign Keys aufgelöst.