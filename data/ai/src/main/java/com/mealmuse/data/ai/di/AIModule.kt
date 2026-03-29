package com.mealmuse.data.ai.di

import com.mealmuse.data.ai.LLMRepositoryImpl
import com.mealmuse.data.ai.LLMSettingsStore
import com.mealmuse.data.ai.provider.AnthropicProvider
import com.mealmuse.data.ai.provider.NIMProvider
import com.mealmuse.data.ai.provider.OpenAIProvider
import com.mealmuse.data.ai.provider.OpenRouterProvider
import com.mealmuse.data.ai.LLMProviderFactory
import com.mealmuse.domain.repository.LLMRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideOpenAIProvider(): OpenAIProvider = OpenAIProvider()

    @Provides
    @Singleton
    fun provideAnthropicProvider(): AnthropicProvider = AnthropicProvider()

    @Provides
    @Singleton
    fun provideOpenRouterProvider(): OpenRouterProvider = OpenRouterProvider()

    @Provides
    @Singleton
    fun provideNIMProvider(): NIMProvider = NIMProvider()

    @Provides
    @Singleton
    fun provideLLMProviderFactory(
        openAI: OpenAIProvider,
        anthropic: AnthropicProvider,
        openRouter: OpenRouterProvider,
        nim: NIMProvider
    ): LLMProviderFactory = LLMProviderFactory(openAI, anthropic, openRouter, nim)

    @Provides
    @Singleton
    fun provideLLMRepository(factory: LLMProviderFactory, settingsStore: LLMSettingsStore): LLMRepository =
        LLMRepositoryImpl(factory, settingsStore)
}
