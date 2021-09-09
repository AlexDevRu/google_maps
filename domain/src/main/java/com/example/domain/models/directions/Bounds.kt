package com.example.domain.models.directions

import com.example.domain.models.Location

data class Bounds(
    val northeast: Location,
    val southwest: Location
)