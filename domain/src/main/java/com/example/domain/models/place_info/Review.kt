package com.example.domain.models.place_info

import java.util.*

data class Review(
    val id: UUID = UUID.randomUUID(),
    val authorName: String? = null,
    val authorUrl: String? = null,
    val profilePhotoUrl: String? = null,
    val rating: Int? = null,
    val relativeTimeDescription: String? = null,
    val text: String? = null,
    val time: Int? = null
)