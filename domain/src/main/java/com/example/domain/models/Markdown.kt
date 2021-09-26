package com.example.domain.models

import com.example.googlemaputil_core.models.Location

data class Markdown(
    val placeId: String,
    val name: String? = null,
    val address: String? = null,
    val location: Location? = null
)