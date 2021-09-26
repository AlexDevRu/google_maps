package com.example.maps

import com.example.data.di.databaseModule
import com.example.data.di.networkModule
import com.example.data.di.useCaseModule
import com.example.maps.di.googleAuthModule
import com.example.maps.di.viewModelModule
import com.github.googlemapfragment.android.GoogleMapApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MapsApplication: GoogleMapApplication(R.string.google_maps_key) {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MapsApplication)
            modules(networkModule, databaseModule, useCaseModule, googleAuthModule, viewModelModule)
        }
    }
}