package com.example.maps.mappers

import com.example.domain.models.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.toModel(): Location {
    return Location(
        latitude, longitude
    )
}