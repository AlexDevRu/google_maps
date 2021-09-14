package com.example.maps.di

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleAuthModule {

    @Provides
    @Singleton
    fun providesGoogleSignInOptions() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("123682947000-5tqfu54kfhj3l8s4v6totgn3ohh4rbjl.apps.googleusercontent.com")
        .requestEmail()
        .build()

    @Provides
    @Singleton
    fun providesGoogleSignInClient(app: Application, gso: GoogleSignInOptions) = GoogleSignIn.getClient(app, gso)

    @Provides
    fun providesLastSignedInAccount(app: Application) = GoogleSignIn.getLastSignedInAccount(app)
}