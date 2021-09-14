package com.example.data.database.converters

import androidx.room.TypeConverter
import com.example.domain.models.Location
import java.util.*

class MapsConverter {
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
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