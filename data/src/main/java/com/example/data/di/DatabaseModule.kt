package com.example.data.di

import android.app.Application
import androidx.room.Room
import com.example.data.api.GoogleMapApiService
import com.example.data.database.MapsDatabase
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.data.repositories.MarkdownRepository
import com.example.domain.repositories.IGoogleMapApiRepository
import com.example.domain.repositories.IMarkdownRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    val DATABASE_NAME = "maps"

    @Provides
    fun providesMapsDatabase(app: Application): MapsDatabase = Room.databaseBuilder(
        app,
        MapsDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    fun providesMarkdownRepository(mapsDatabase: MapsDatabase): IMarkdownRepository = MarkdownRepository(mapsDatabase.mapsDao())

    @Provides
    fun providesGoogleMapApiRepository(service: GoogleMapApiService): IGoogleMapApiRepository = GoogleMapsApiRepository(service)
}