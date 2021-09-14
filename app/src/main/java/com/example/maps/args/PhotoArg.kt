package com.example.maps.args

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoArg(
    val height: Int?,
    val htmlAttributions: List<String>?,
    val photoReference: String?,
    val width: Int?
): Parcelable