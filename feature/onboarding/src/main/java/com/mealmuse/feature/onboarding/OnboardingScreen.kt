package com.mealmuse.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val slides = listOf(
    OnboardingSlide(
        icon = Icons.Default.CalendarMonth,
        title = "Welcome to MealMuse",
        description = "Your AI-powered meal planning companion. Generate personalized weekly meal plans based on your dietary preferences and available ingredients."
    ),
    OnboardingSlide(
        icon = Icons.Default.Tune,
        title = "Set Your Preferences",
        description = "Choose from 6 dietary modes — Keto, Low-Carb, Vegetarian, Vegan, Paleo, or Calorie Deficit. Or create your own custom mode."
    ),
    OnboardingSlide(
        icon = Icons.Default.Key,
        title = "Configure AI",
        description = "Enter your API key for OpenAI, Anthropic, OpenRouter, or NIM in Settings. Your key stays on your device — this is a private app."
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    val slide = slides[currentSlide]

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                slide.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                slide.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                slide.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slides.indices.forEach { index ->
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (index == currentSlide) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    ) {}
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalArrangement = if (currentSlide == 0) {
                Arrangement.End
            } else {
                Arrangement.SpaceBetween
            }
        ) {
            if (currentSlide > 0) {
                OutlinedButton(onClick = { currentSlide-- }) {
                    Text("Back")
                }
            }

            if (currentSlide < slides.lastIndex) {
                Button(onClick = { currentSlide++ }) {
                    Text("Next")
                }
            } else {
                Button(onClick = onComplete) {
                    Text("Get Started")
                }
            }
        }
    }
}
