package com.example.domain.repositories

import com.example.domain.models.Markdown
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface IMarkdownRepository {
    fun getMarkdowns(): Single<List<Markdown>>
    fun isPlaceInMarkdowns(id: String): Maybe<Markdown>
    fun deleteMarkdownById(id: String): Completable
    fun insert(markdown: Markdown): Completable
}