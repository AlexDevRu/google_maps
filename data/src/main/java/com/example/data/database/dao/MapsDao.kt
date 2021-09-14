package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.database.entities.MarkdownEntity
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

@Dao
interface MapsDao {
    @Query("select * from markdowns")
    fun getMarkdowns(): Single<List<MarkdownEntity>>

    @Query("delete from markdowns where id=:id")
    fun deleteMarkdownById(id: UUID)

    @Insert
    fun insert(markdownEntity: MarkdownEntity)
}