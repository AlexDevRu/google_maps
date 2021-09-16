package com.example.domain.repositories

import com.example.domain.models.Markdown
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface IFirebaseRepository {
    fun insertMarkdown(markdown: Markdown): Completable
    fun deleteMarkdown(id: String): Completable
    fun getMarkdowns(): Single<List<Markdown>>
    fun isPlaceInMarkdowns(id: String): Single<Markdown>
}