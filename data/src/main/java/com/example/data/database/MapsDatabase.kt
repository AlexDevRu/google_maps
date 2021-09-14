package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.database.converters.MapsConverter
import com.example.data.database.dao.MapsDao
import com.example.data.database.entities.MarkdownEntity

@Database(
    entities = [
        MarkdownEntity::class,
    ],
    version = 1
)
@TypeConverters(MapsConverter::class)
abstract class MapsDatabase : RoomDatabase() {
    abstract fun mapsDao(): MapsDao
}