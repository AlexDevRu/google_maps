package com.example.domain.use_cases.markdowns

import com.example.domain.models.Markdown
import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Completable

class InsertMarkdownUseCase(
    private val markdownRepository: IMarkdownRepository,
    private val firebaseRepository: IFirebaseRepository
) {
    fun invoke(markdown: Markdown): Completable {
        return firebaseRepository.insertMarkdown(markdown)
    }
}