package com.example.domain.models.directions

data class Direction(
    val bounds: Bounds? = null,
    val routes: List<Route>? = null,
    val status: String
)