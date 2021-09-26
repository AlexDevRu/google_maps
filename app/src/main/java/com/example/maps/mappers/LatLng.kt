package com.example.maps.mappers

import com.github.core.models.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.toModel(): Location {
    return Location(
        latitude, longitude
    )
}

fun Location.toArg(): LatLng {
    return LatLng(lat, lng)
}
