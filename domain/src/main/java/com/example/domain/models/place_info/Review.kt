package com.example.domain.models.place_info

import java.util.*

data class Review(
    val id: UUID = UUID.randomUUID(),
    val authorName: String,
    val authorUrl: String? = null,
    val profilePhotoUrl: String? = null,
    val rating: Int,
    val relativeTimeDescription: String,
    val text: String? = null,
    val time: Int
)