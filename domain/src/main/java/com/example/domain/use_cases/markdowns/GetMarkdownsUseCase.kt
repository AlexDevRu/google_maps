package com.example.domain.use_cases.markdowns

import com.example.domain.models.Markdown
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Single

class GetMarkdownsUseCase(private val markdownRepository: IMarkdownRepository) {
    fun invoke(): Single<List<Markdown>> {
        return markdownRepository.getMarkdowns()
    }
}