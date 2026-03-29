package com.mealmuse.domain.repository

import com.mealmuse.core.common.Result
import com.mealmuse.domain.model.Recipe

interface RecipeSearchRepository {
    suspend fun searchWeb(query: String): Result<List<Recipe>>
    suspend fun fetchByUrl(url: String): Result<Recipe>
}
