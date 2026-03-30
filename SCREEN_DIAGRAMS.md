# MealMuse - Screen-Aufbau-Diagramme

Dieses Dokument skizziert das visuelle Layout und den strukturellen Aufbau der einzelnen Screens in der App (Jetpack Compose Layouts).

## 1. Meal Plan Screen
```text
+---------------------------------------------------+
| [TopAppBar]  Meal Plan              [Pref] [Set]  |
+---------------------------------------------------+
| [Row]  (<) Prev Week   | Week 1 |   Next Week (>) |
+---------------------------------------------------+
| [LazyRow] (Mon) (Tue) (Wed) (Thu) (Fri) (Sat) (Sun)|
+---------------------------------------------------+
|                                                   |
| [LazyColumn]                                      |
|  +---------------------------------------------+  |
|  | [MealSlot] Breakfast                        |  |
|  | Recipe Name                     [350 kcal]  |  |
|  +---------------------------------------------+  |
|  +---------------------------------------------+  |
|  | [MealSlot] Lunch                            |  |
|  | Recipe Name                     [600 kcal]  |  |
|  +---------------------------------------------+  |
|  ... (Dinner, Snack)                              |
|                                                   |
+---------------------------------------------------+
|                                             (FAB) |
|                                          [Generate|
|                                             Plan] |
+---------------------------------------------------+
| [BottomNavBar] [Meal Plan]  [Cookbook]  [Fridge]  |
+---------------------------------------------------+
```

## 2. Fridge Screen
```text
+---------------------------------------------------+
| [TopAppBar]  Fridge                 [Pref] [Set]  |
+---------------------------------------------------+
| [SearchBar] 🔍 Search ingredients...              |
+---------------------------------------------------+
| [LazyRow] (All) (Produce) (Dairy) (Protein) ...   |
+---------------------------------------------------+
|                                                   |
| [LazyColumn]                                      |
|  +---------------------------------------------+  |
|  | [Row] 🍅 Tomatoes           [Badge: Produce]|  |
|  |       500 g                 [Icon: Expiring]|  |
|  +---------------------------------------------+  |
|  +---------------------------------------------+  |
|  | [Row] 🥛 Milk               [Badge: Dairy]  |  |
|  |       1 Liter                               |  |
|  +---------------------------------------------+  |
|                                                   |
+---------------------------------------------------+
|                                             (FAB) |
|                                               [+] |
+---------------------------------------------------+
| [BottomNavBar] [Meal Plan]  [Cookbook]  [Fridge]  |
+---------------------------------------------------+
```

## 3. Recipe Book Screen
```text
+---------------------------------------------------+
| [TopAppBar]  Cookbook               [Pref] [Set]  |
+---------------------------------------------------+
| [SearchBar] 🔍 Search recipes...                  |
+---------------------------------------------------+
| [LazyRow] (All) (Breakfast) (High Protein) ...    |
+---------------------------------------------------+
|                                                   |
| [LazyVerticalGrid - 2 Columns]                    |
|  +--------------------+  +--------------------+   |
|  | [Image Placeholder]|  | [Image Placeholder]|   |
|  | Avocado Toast      |  | Chicken Salad      |   |
|  | 350 kcal | 15g P   |  | 450 kcal | 40g P   |   |
|  | (Tag: Vegan)       |  | (Tag: Keto)        |   |
|  +--------------------+  +--------------------+   |
|                                                   |
+---------------------------------------------------+
|                                             (FAB) |
|                                      [✨ AI Sugg.] |
+---------------------------------------------------+
| [BottomNavBar] [Meal Plan]  [Cookbook]  [Fridge]  |
+---------------------------------------------------+
```

## 4. AI Suggest Screen
```text
+---------------------------------------------------+
| [TopAppBar] (<) AI Suggest                        |
+---------------------------------------------------+
| [TabRow]    [ Research ]    |    [ Improve ]      |
+---------------------------------------------------+
| [SearchBar] 🔍 Ask for a recipe idea...           |
|                                                   |
| [LazyColumn] (AI Results)                         |
|  +---------------------------------------------+  |
|  | [ResultCard]                                |  |
|  | Recipe Name                      [Score: 95]|  |
|  | Description...                              |  |
|  | [Button: Save to Cookbook]                  |  |
|  +---------------------------------------------+  |
|                                                   |
+---------------------------------------------------+
```

## 5. Preferences Screen
```text
+---------------------------------------------------+
| [TopAppBar] (<) Preferences                       |
+---------------------------------------------------+
| [Column]                                          |
|  Dietary Mode                                     |
|  [FlowRow]                                        |
|   (Keto) (Vegan) (Paleo) (Balanced) (Custom)      |
|                                                   |
|  Daily Macro Goals                                |
|  Calories: 2000 kcal                              |
|  [=========O================]                     |
|                                                   |
|  Protein: 150g                                    |
|  [==================O=======]                     |
|                                                   |
|  Carbs: 200g                                      |
|  [=============O============]                     |
|                                                   |
|  Fat: 70g                                         |
|  [======O===================]                     |
|                                                   |
|  [Button: Save Preferences]                       |
+---------------------------------------------------+
```

## 6. Settings Screen
```text
+---------------------------------------------------+
| [TopAppBar] (<) Settings                          |
+---------------------------------------------------+
| [Column]                                          |
|  AI Provider Setup                                |
|                                                   |
|  [Dropdown] Select Provider                       |
|  | NVIDIA NIM (Selected) |                        |
|                                                   |
|  [TextField] API Key                              |
|  | *********************                 [👁] |   |
|                                                   |
|  [Dropdown] Select Model                          |
|  | meta/llama-3.1-70b-instruct |                  |
|                                                   |
|  [Row]                                            |
|   [Button: Test Key]    [Icon: Success/Fail]      |
|                                                   |
|  [Button: Save Settings]                          |
+---------------------------------------------------+
```