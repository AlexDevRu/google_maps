package com.example.domain.use_cases.markdowns

import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Completable

class DeleteMarkdownByIdUseCase(private val markdownRepository: IMarkdownRepository) {
    fun invoke(id: String): Completable {
        return markdownRepository.deleteMarkdownById(id)
    }
}