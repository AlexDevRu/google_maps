package com.example.data.api.responses.directions

data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    val overview_polyline: Polyline,
    val summary: String,
    val warnings: List<Any>? = null,
    val waypoint_order: List<Any>? = null
)