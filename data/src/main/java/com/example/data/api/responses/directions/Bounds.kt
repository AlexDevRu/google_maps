package com.example.data.api.responses.directions

import com.example.data.api.responses.Location

data class Bounds(
    val northeast: Location,
    val southwest: Location
)