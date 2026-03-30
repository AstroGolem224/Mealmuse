package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getPreferences(): Flow<UserPreferences>
    suspend fun getPreferencesOnce(): UserPreferences
    suspend fun savePreferences(preferences: UserPreferences): Result<Unit>
}
