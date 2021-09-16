package com.example.data.di

import com.example.domain.repositories.IGoogleMapApiRepository
import com.example.domain.repositories.IMarkdownRepository
import com.example.domain.use_cases.GetDirectionUseCase
import com.example.domain.use_cases.GetInfoByLocationUseCase
import com.example.domain.use_cases.markdowns.DeleteMarkdownByIdUseCase
import com.example.domain.use_cases.markdowns.GetMarkdownsUseCase
import com.example.domain.use_cases.markdowns.InsertMarkdownUseCase
import com.example.domain.use_cases.markdowns.IsPlaceInMarkdownsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun providesGetDirectionUseCase(googleMapsApiRepository: IGoogleMapApiRepository)
            = GetDirectionUseCase(googleMapsApiRepository)

    @Provides
    fun providesGetInfoByLocationUseCase(googleMapsApiRepository: IGoogleMapApiRepository)
            = GetInfoByLocationUseCase(googleMapsApiRepository)


    @Provides
    fun providesDeleteMarkdownByIdUseCase(markdownRepository: IMarkdownRepository)
            = DeleteMarkdownByIdUseCase(markdownRepository)

    @Provides
    fun providesGetMarkdownsUseCase(markdownRepository: IMarkdownRepository)
            = GetMarkdownsUseCase(markdownRepository)

    @Provides
    fun providesInsertMarkdownUseCase(markdownRepository: IMarkdownRepository)
            = InsertMarkdownUseCase(markdownRepository)

    @Provides
    fun providesIsPlaceInMarkdownsUseCase(markdownRepository: IMarkdownRepository)
            = IsPlaceInMarkdownsUseCase(markdownRepository)
}