package com.example.maps.di

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val googleAuthModule = module {
    single {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("123682947000-5tqfu54kfhj3l8s4v6totgn3ohh4rbjl.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    single {
        GoogleSignIn.getClient(androidContext(), get())
    }

    single {
        GoogleSignIn.getLastSignedInAccount(get())
    }
}