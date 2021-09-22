package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        private const val DATABASE_NAME = "maps"

        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MapsDatabase? = null

        fun getDatabase(context: Context): MapsDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapsDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}