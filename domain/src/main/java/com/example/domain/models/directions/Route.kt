package com.example.domain.models.directions

data class Route(
    val legs: List<Leg>,
    val overview_polyline: Polyline,
    val summary: String
)