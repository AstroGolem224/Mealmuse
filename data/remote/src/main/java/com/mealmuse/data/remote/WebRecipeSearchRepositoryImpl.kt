package com.mealmuse.data.remote

import com.mealmuse.core.common.Result
import com.mealmuse.core.common.suspendResult
import com.mealmuse.domain.model.Recipe
import com.mealmuse.domain.repository.RecipeSearchRepository
import javax.inject.Inject

class WebRecipeSearchRepositoryImpl @Inject constructor() : RecipeSearchRepository {
    override suspend fun searchWeb(query: String): Result<List<Recipe>> = suspendResult {
        // TODO: Implement web scraping with OkHttp + Jsoup
        throw NotImplementedError("Web scraping pending")
    }

    override suspend fun fetchByUrl(url: String): Result<Recipe> = suspendResult {
        // TODO: Implement recipe fetch from URL
        throw NotImplementedError("Recipe fetch pending")
    }
}
