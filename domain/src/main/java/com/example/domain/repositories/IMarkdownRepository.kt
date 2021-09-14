package com.example.domain.repositories

import com.example.domain.models.Markdown
import io.reactivex.Single
import java.util.*

interface IMarkdownRepository {
    fun getMarkdowns(): Single<List<Markdown>>
    fun deleteMarkdownById(id: UUID)
    fun insert(markdown: Markdown)
}