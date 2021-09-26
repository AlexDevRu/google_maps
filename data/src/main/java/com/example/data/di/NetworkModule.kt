package com.example.data.di

import com.example.data.repositories.FirebaseRepository
import com.example.domain.repositories.IFirebaseRepository
import org.koin.dsl.module

val networkModule = module {
    single<IFirebaseRepository> {
        FirebaseRepository()
    }
}