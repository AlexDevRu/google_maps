package com.example.data.repositories

import com.example.data.database.dao.MapsDao
import com.example.data.mappers.fromModel
import com.example.data.mappers.toModel
import com.example.domain.models.Markdown
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import javax.inject.Inject

class MarkdownRepository @Inject constructor(private val markdownDao: MapsDao): IMarkdownRepository {

    override fun getMarkdowns(): Single<List<Markdown>> {
        return markdownDao.getMarkdowns().subscribeOn(Schedulers.io())
            .map { it ->
                it.map { it.toModel() }
            }
    }

    override fun isPlaceInMarkdowns(id: String): Maybe<Markdown> {
        return markdownDao.getMarkdownById(id).subscribeOn(Schedulers.io())
            .map {
                it.toModel()
            }
    }

    override fun deleteMarkdownById(id: String): Completable {
        return markdownDao.deleteMarkdownById(id).subscribeOn(Schedulers.io())
    }

    override fun insert(markdown: Markdown): Completable {
        return markdownDao.insert(markdown.fromModel()).subscribeOn(Schedulers.io())
    }

}