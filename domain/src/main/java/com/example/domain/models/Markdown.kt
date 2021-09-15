package com.example.domain.models

data class Markdown(
    val placeId: String,
    val name: String? = null,
    val address: String? = null,
    val location: Location? = null
)