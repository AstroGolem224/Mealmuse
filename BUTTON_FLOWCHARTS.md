# MealMuse - Flowcharts für Buttons und Optionen

Dieses Dokument beschreibt die Interaktionen und den erwarteten Programmfluss ("Expected Outcome") für jeden Button und jede Option in der App.

## 1. Onboarding Screen
*   **Button: "Get Started" / "Next"**
    *   *Aktion:* Validiert die Eingabe (z.B. API-Key) und speichert die Ersteinrichtung.
    *   *Expected Outcome:* Flag `onboarding_completed` wird in SharedPreferences auf `true` gesetzt. Navigation zum `MealPlanScreen`.

## 2. Global Navigation (Bottom Navigation Bar)
*   **Tab: "Meal Plan"**
    *   *Expected Outcome:* Navigiert zum `MealPlanScreen`.
*   **Tab: "Cookbook"**
    *   *Expected Outcome:* Navigiert zum `RecipeBookScreen`.
*   **Tab: "Fridge"**
    *   *Expected Outcome:* Navigiert zum `FridgeScreen`.

## 3. Top App Bar (Global)
*   **Icon: "Preferences" (Tune-Icon)**
    *   *Expected Outcome:* Öffnet den `PreferencesScreen` (Dietary Mode & Macros).
*   **Icon: "Settings" (Gear-Icon)**
    *   *Expected Outcome:* Öffnet den `SettingsScreen` (API Keys & LLM Provider).

## 4. Meal Plan Screen
*   **Button: "Generate Plan" (FAB oder Empty State CTA)**
    *   *Flow:* Prüft Preferences & Fridge -> Baut Prompt -> Sendet an LLM -> Parst Antwort -> Speichert in DB -> Updated UI.
    *   *Expected Outcome:* Zeigt Lade-Indikator, danach wird ein neuer 7-Tage-Mahlzeitenplan angezeigt.
*   **Buttons: "Previous Week" (<) / "Next Week" (>)**
    *   *Expected Outcome:* Lädt den Plan für die entsprechende Woche aus der Datenbank und aktualisiert die Ansicht.
*   **Option: "Day Selector" (Mon - Sun Chips)**
    *   *Expected Outcome:* Filtert die angezeigten Mahlzeiten (Frühstück, Mittag, Abend, Snack) für den ausgewählten Tag.
*   **Button: "Retry" (bei Fehler)**
    *   *Expected Outcome:* Startet den Generierungsprozess erneut.

## 5. Fridge Screen
*   **Button: "Add Ingredient" (FAB)**
    *   *Expected Outcome:* Öffnet den Dialog zum Hinzufügen einer neuen Zutat.
*   **Button: "Save" (im Add Dialog)**
    *   *Expected Outcome:* Validiert Eingabe -> Speichert in Room DB -> Zutat taucht in der Liste auf.
*   **Option: "Category Filter Chips" (Produce, Dairy, etc.)**
    *   *Expected Outcome:* Filtert die Zutatenliste sofort nach der gewählten Kategorie.
*   **Aktion: "Swipe left on Ingredient"**
    *   *Expected Outcome:* Löscht die Zutat aus der Datenbank (mit kurzem Undo-Snackbar).

## 6. Recipe Book Screen
*   **Option: "Search Bar"**
    *   *Expected Outcome:* Filtert Rezepte in Echtzeit nach Namen oder Zutat (Debounced).
*   **Button: "Recipe Card" (Click)**
    *   *Expected Outcome:* Navigiert zum `RecipeDetailScreen` für das entsprechende Rezept.
*   **Button: "AI Suggest" (FAB)**
    *   *Expected Outcome:* Navigiert zum `AISuggestScreen` für KI-gestützte Rezept-Recherche.

## 7. AI Suggest Screen
*   **Tab: "Research"**
    *   *Button: "Search"* -> *Outcome:* Sendet Query an KI -> Zeigt gefundene Rezeptideen.
    *   *Button: "Save to Cookbook"* -> *Outcome:* Speichert das KI-generierte Rezept in der lokalen DB.
*   **Tab: "Improve"**
    *   *Button: "Select Recipe"* -> *Outcome:* Öffnet Auswahl für ein bestehendes Rezept.
    *   *Button: "Improve"* -> *Outcome:* KI generiert eine gesündere/bessere Variante -> Zeigt Vorher/Nachher-Diff.

## 8. Preferences & Settings Screens
*   **Option: "Dietary Mode Chips" (Keto, Vegan, etc.)**
    *   *Expected Outcome:* Ändert die Makro-Slider auf vordefinierte Werte.
*   **Button: "Save Preferences"**
    *   *Expected Outcome:* Speichert die Vorgaben in der Room DB.
*   **Button: "Test Key" (Settings)**
    *   *Expected Outcome:* Sendet einen Ping an den gewählten Provider. Zeigt "Erfolg" (Grün) oder Fehlermeldung (Rot).