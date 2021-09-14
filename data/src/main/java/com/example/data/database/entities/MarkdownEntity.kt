package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.models.Location
import java.util.*

@Entity(tableName = "markdowns")
data class MarkdownEntity(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val name: String? = null,
    val address: String? = null,
    val location: Location? = null
)