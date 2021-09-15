package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entities.MarkdownEntity
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface MapsDao {
    @Query("select * from markdowns")
    fun getMarkdowns(): Single<List<MarkdownEntity>>

    @Query("delete from markdowns where placeId=:id")
    fun deleteMarkdownById(id: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(markdownEntity: MarkdownEntity): Completable

    @Query("select * from markdowns where placeId=:id")
    fun getMarkdownById(id: String): Maybe<MarkdownEntity>
}