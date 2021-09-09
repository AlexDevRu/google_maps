package com.example.data.api.responses.directions

data class DirectionResponse(
    val bounds: Bounds? = null,
    val geocoded_waypoints: List<GeocodedWaypoint>? = null,
    val routes: List<Route>? = null,
    val status: String
)