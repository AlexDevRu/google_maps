package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.core.models.Location

@Entity(tableName = "markdowns")
data class MarkdownEntity(
    @PrimaryKey
    val placeId: String,
    val name: String? = null,
    val address: String? = null,
    val location: Location? = null
)