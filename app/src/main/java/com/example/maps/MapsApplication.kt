package com.example.maps

import android.app.Application
import com.example.data.di.databaseModule
import com.example.data.di.networkModule
import com.example.data.di.useCaseModule
import com.example.maps.di.googleAuthModule
import com.example.maps.di.viewModelModule
import com.google.android.libraries.places.api.Places
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MapsApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        startKoin {
            androidContext(this@MapsApplication)
            modules(networkModule, databaseModule, useCaseModule, googleAuthModule, viewModelModule)
        }
    }
}