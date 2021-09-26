package com.example.maps.mappers

import com.example.googlemaputil_core.models.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.toModel(): Location {
    return Location(
        latitude, longitude
    )
}

fun Location.toArg(): LatLng {
    return LatLng(lat, lng)
}
