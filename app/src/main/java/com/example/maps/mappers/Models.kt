package com.example.maps.mappers

import com.example.domain.models.place_info.Photo
import com.example.maps.args.PhotoArg

fun Photo.toArg(): PhotoArg {
    return PhotoArg(
        height = height,
        width = width,
        htmlAttributions = htmlAttributions,
        photoReference = photoReference
    )
}