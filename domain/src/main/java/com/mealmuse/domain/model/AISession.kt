package com.mealmuse.domain.model

data class AISession(
    val id: String,
    val type: AISessionType,
    val prompt: String,
    val response: String,
    val provider: String,
    val createdAt: Long,
    val tokenCount: Int
)

enum class AISessionType {
    MEAL_PLAN,
    RESEARCH,
    IMPROVEMENT
}
