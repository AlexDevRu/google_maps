package com.example.data.mappers

import com.example.data.database.entities.MarkdownEntity
import com.example.domain.models.Markdown

fun MarkdownEntity.toModel(): Markdown {
    return Markdown(
        placeId = placeId,
        name = name,
        address = address,
        location = location
    )
}

fun Markdown.fromModel(): MarkdownEntity {
    return MarkdownEntity(
        placeId = placeId,
        name = name,
        address = address,
        location = location
    )
}