package com.example.domain.models.directions

data class Direction(
    val bounds: Bounds? = null,
    val routes: List<Route>? = null,
    val legs: List<Leg>? = null,
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val status: String
)