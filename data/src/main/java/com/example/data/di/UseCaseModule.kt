package com.example.data.di

import com.example.domain.use_cases.GetDirectionUseCase
import com.example.domain.use_cases.GetInfoByLocationUseCase
import com.example.domain.use_cases.markdowns.DeleteMarkdownByIdUseCase
import com.example.domain.use_cases.markdowns.GetMarkdownsUseCase
import com.example.domain.use_cases.markdowns.InsertMarkdownUseCase
import com.example.domain.use_cases.markdowns.IsPlaceInMarkdownsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single {
        GetDirectionUseCase(get())
    }

    single {
        GetInfoByLocationUseCase(get())
    }

    single {
        DeleteMarkdownByIdUseCase(get(), get())
    }

    single {
        GetMarkdownsUseCase(get(), get())
    }

    single {
        InsertMarkdownUseCase(get(), get())
    }

    single {
        IsPlaceInMarkdownsUseCase(get(), get())
    }
}