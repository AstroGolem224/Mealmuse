package com.mealmuse.data.ai

import android.content.Context
import android.content.SharedPreferences
import com.mealmuse.domain.model.LLMProvider
import com.mealmuse.domain.model.LLMSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LLMSettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("llm_settings", Context.MODE_PRIVATE)

    fun getSettings(): LLMSettings {
        val provider = prefs.getString("provider", null)
            ?: return LLMSettings(
                provider = LLMProvider.OPENAI,
                apiKey = "",
                model = "gpt-4o-mini",
                isActive = false
            )

        return LLMSettings(
            provider = try { LLMProvider.valueOf(provider) } catch (e: Exception) { LLMProvider.OPENAI },
            apiKey = prefs.getString("api_key", "") ?: "",
            model = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini",
            baseUrl = prefs.getString("base_url", null),
            isActive = prefs.getBoolean("is_active", false)
        )
    }

    fun saveSettings(settings: LLMSettings) {
        prefs.edit()
            .putString("provider", settings.provider.name)
            .putString("api_key", settings.apiKey)
            .putString("model", settings.model)
            .putString("base_url", settings.baseUrl)
            .putBoolean("is_active", settings.isActive)
            .apply()
    }
}