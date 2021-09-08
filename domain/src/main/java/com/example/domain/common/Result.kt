package com.example.domain.common

sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val throwable: Throwable) : Result<T>()
    data class Loading<T>(val loading: Boolean = true) : Result<T>()
}
