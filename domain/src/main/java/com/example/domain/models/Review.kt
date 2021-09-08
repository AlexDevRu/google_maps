package com.example.domain.models

import java.util.*

data class Review(
    val id: UUID = UUID.randomUUID(),
    val authorName: String,
    val authorUrl: String,
    val profilePhotoUrl: String,
    val rating: Int,
    val relativeTimeDescription: String,
    val text: String,
    val time: Int
)