package com.example.maps.args

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarkdownArg (
    val placeId: String,
    val name: String? = null,
    val address: String? = null,
    val location: LocationArg? = null
): Parcelable

@Parcelize
data class LocationArg (
    val lat: Double,
    val lng: Double
): Parcelable