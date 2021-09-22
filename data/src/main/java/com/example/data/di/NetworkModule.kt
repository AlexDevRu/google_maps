package com.example.data.di

import com.example.data.api.ApiConstants
import com.example.data.api.GoogleMapApiService
import com.example.data.repositories.FirebaseRepository
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IGoogleMapApiRepository
import org.koin.dsl.binds
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(GoogleMapApiService::class.java)
    }

    single<IGoogleMapApiRepository> {
        GoogleMapsApiRepository(get())
    }

    single<IFirebaseRepository> {
        FirebaseRepository()
    }
}