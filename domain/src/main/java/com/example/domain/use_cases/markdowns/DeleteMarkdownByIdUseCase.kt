package com.example.domain.use_cases.markdowns

import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Completable

class DeleteMarkdownByIdUseCase(
    private val markdownRepository: IMarkdownRepository,
    private val firebaseRepository: IFirebaseRepository
) {
    fun invoke(id: String): Completable {
        return firebaseRepository.deleteMarkdown(id)
    }
}