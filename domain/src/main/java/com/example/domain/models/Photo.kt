package com.example.domain.models

data class Photo(
    val height: Int,
    val htmlAttributions: List<String>,
    val photoReference: String,
    val width: Int
)