package com.example.domain.models.directions

data class Direction(
    val bounds: Bounds? = null,
    val routes: List<Route>? = null,
    val legs: List<Leg>? = null,
    val total_distance: Distance?,
    val total_duration: Duration?,
    val status: String
)