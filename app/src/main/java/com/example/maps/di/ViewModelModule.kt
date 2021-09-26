package com.example.maps.di

import com.example.maps.ui.GlobalVM
import com.example.maps.ui.fragments.authorization.sign_in.SignInVM
import com.example.maps.ui.fragments.main.MainVM
import com.example.maps.ui.fragments.markdowns.MarkdownsVM
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SignInVM()
    }

    viewModel {
        MainVM(androidApplication(), get(), get(), get())
    }

    viewModel {
        MarkdownsVM(get(), get())
    }

    viewModel {
        GlobalVM(get())
    }
}