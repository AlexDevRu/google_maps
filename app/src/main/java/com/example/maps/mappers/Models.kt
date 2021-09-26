package com.example.maps.mappers

import com.example.domain.models.Markdown
import com.example.maps.args.MarkdownArg
import com.example.maps.args.PhotoArg
import com.github.core.models.place_info.Photo

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
