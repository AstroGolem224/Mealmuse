package com.mealmuse.data.remote.di

import com.mealmuse.data.remote.WebRecipeSearchRepositoryImpl
import com.mealmuse.domain.repository.RecipeSearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideRecipeSearchRepository(): RecipeSearchRepository =
        WebRecipeSearchRepositoryImpl()
}
