package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.AISessionEntity
import com.mealmuse.domain.model.AISession
import com.mealmuse.domain.model.AISessionType

fun AISessionEntity.toDomain(): AISession = AISession(
    id = id,
    type = try {
        AISessionType.valueOf(type)
    } catch (e: Exception) {
        AISessionType.MEAL_PLAN
    },
    prompt = prompt,
    response = response,
    provider = provider,
    createdAt = createdAt,
    tokenCount = tokenCount
)

fun AISession.toEntity(): AISessionEntity = AISessionEntity(
    id = id,
    type = type.name,
    prompt = prompt,
    response = response,
    provider = provider,
    createdAt = createdAt,
    tokenCount = tokenCount
)
