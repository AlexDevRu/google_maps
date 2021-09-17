package com.example.domain.models.directions

data class Direction(
    val bounds: Bounds? = null,
    val routes: List<Route>? = null,
    val legs: List<Leg>? = null,
    val total_distance: Int = 0,
    val total_duration: Int = 0,
    val status: String
)