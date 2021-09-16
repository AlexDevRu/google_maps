package com.example.data.di

import com.example.domain.repositories.IFirebaseRepository
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
    fun providesDeleteMarkdownByIdUseCase(markdownRepository: IMarkdownRepository, firebaseRepository: IFirebaseRepository)
            = DeleteMarkdownByIdUseCase(markdownRepository, firebaseRepository)

    @Provides
    fun providesGetMarkdownsUseCase(markdownRepository: IMarkdownRepository, firebaseRepository: IFirebaseRepository)
            = GetMarkdownsUseCase(markdownRepository, firebaseRepository)

    @Provides
    fun providesInsertMarkdownUseCase(markdownRepository: IMarkdownRepository, firebaseRepository: IFirebaseRepository)
            = InsertMarkdownUseCase(markdownRepository, firebaseRepository)

    @Provides
    fun providesIsPlaceInMarkdownsUseCase(markdownRepository: IMarkdownRepository, firebaseRepository: IFirebaseRepository)
            = IsPlaceInMarkdownsUseCase(markdownRepository, firebaseRepository)
}