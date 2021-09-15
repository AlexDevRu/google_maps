package com.example.maps.mappers

import com.example.domain.models.Location
import com.example.domain.models.Markdown
import com.example.domain.models.place_info.Photo
import com.example.maps.args.LocationArg
import com.example.maps.args.MarkdownArg
import com.example.maps.args.PhotoArg

fun Photo.toArg(): PhotoArg {
    return PhotoArg(
        height = height,
        width = width,
        htmlAttributions = htmlAttributions,
        photoReference = photoReference
    )
}

fun Markdown.toArg(): MarkdownArg {
    return MarkdownArg(
        placeId = placeId,
        name = name,
        address = address,
        location = location?.toArg()
    )
}

fun MarkdownArg.toModel(): Markdown {
    return Markdown(
        placeId = placeId,
        name = name,
        address = address,
        location = location?.toModel()
    )
}

fun Location.toArg(): LocationArg {
    return LocationArg(
        lat = lat,
        lng = lng
    )
}

fun LocationArg.toModel(): Location {
    return Location(
        lat = lat,
        lng = lng
    )
}