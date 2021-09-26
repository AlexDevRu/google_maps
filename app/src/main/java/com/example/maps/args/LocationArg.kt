package com.example.maps.args

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarkdownArg (
    val placeId: String,
    val name: String? = null,
    val address: String? = null,
    val location: LatLng? = null
): Parcelable
