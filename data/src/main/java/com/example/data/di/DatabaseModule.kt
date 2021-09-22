package com.example.data.di

import com.example.data.database.MapsDatabase
import com.example.data.repositories.FirebaseRepository
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.data.repositories.MarkdownRepository
import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IGoogleMapApiRepository
import com.example.domain.repositories.IMarkdownRepository
import org.koin.dsl.binds
import org.koin.dsl.module

val databaseModule = module {
    single {
        MapsDatabase.getDatabase(get())
    }

    single<IMarkdownRepository> {
        MarkdownRepository(get<MapsDatabase>().mapsDao())
    }
}