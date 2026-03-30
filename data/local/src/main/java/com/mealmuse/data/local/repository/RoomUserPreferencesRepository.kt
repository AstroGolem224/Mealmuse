package com.mealmuse.data.local.repository

import com.mealmuse.core.common.Result
import com.mealmuse.data.local.dao.PreferencesDao
import com.mealmuse.data.local.mapper.toDomain
import com.mealmuse.data.local.mapper.toEntity
import com.mealmuse.domain.model.UserPreferences
import com.mealmuse.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomUserPreferencesRepository @Inject constructor(
    private val preferencesDao: PreferencesDao
) : UserPreferencesRepository {

    override fun getPreferences(): Flow<UserPreferences> {
        return preferencesDao.get().map { entity ->
            entity?.toDomain() ?: UserPreferences(dietaryModes = emptyList())
        }
    }

    override suspend fun getPreferencesOnce(): UserPreferences {
        return preferencesDao.getOnce()?.toDomain()
            ?: UserPreferences(dietaryModes = emptyList())
    }

    override suspend fun savePreferences(preferences: UserPreferences): Result<Unit> {
        return try {
            preferencesDao.insertOrUpdate(preferences.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}
