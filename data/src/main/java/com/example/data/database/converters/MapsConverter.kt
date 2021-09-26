package com.example.data.database.converters

import androidx.room.TypeConverter
import com.github.core.models.Location

class MapsConverter {
    @TypeConverter
    fun toLocation(location: String): Location {
        return Location(
            location.split(";").first().toDouble(),
            location.split(";").last().toDouble()
        )
    }
    @TypeConverter
    fun fromLocation(location: Location): String {
        return "${location.lat};${location.lng}"
    }
}