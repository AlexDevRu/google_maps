package com.example.maps.di

import com.example.data.api.GoogleMapApiService
import com.example.data.repositories.GoogleMapsApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun providesGoogleMapApiRepository(service: GoogleMapApiService) = GoogleMapsApiRepository(service)
}