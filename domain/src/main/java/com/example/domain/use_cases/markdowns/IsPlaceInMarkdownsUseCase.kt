package com.example.domain.use_cases.markdowns

import com.example.domain.models.Markdown
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Maybe

class IsPlaceInMarkdownsUseCase(private val markdownRepository: IMarkdownRepository) {
    fun invoke(id: String): Maybe<Markdown> {
        return markdownRepository.isPlaceInMarkdowns(id)
    }
}