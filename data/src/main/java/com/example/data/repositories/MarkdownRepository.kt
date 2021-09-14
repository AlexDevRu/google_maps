package com.example.data.repositories

import com.example.data.database.dao.MapsDao
import com.example.data.mappers.fromModel
import com.example.data.mappers.toModel
import com.example.domain.models.Markdown
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MarkdownRepository @Inject constructor(private val markdownDao: MapsDao): IMarkdownRepository {
    override fun getMarkdowns(): Single<List<Markdown>> {
        return markdownDao.getMarkdowns()
            .map { it ->
                it.map { it.toModel() }
            }
    }

    override fun deleteMarkdownById(id: UUID) {
        markdownDao.deleteMarkdownById(id)
    }

    override fun insert(markdown: Markdown) {
        markdownDao.insert(markdown.fromModel())
    }

}