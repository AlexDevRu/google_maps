package com.example.domain.use_cases.markdowns

import com.example.domain.models.Markdown
import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Single

class GetMarkdownsUseCase(
    private val markdownRepository: IMarkdownRepository,
    private val firebaseRepository: IFirebaseRepository
) {
    fun invoke(): Single<List<Markdown>> {
        return firebaseRepository.getMarkdowns()//markdownRepository.getMarkdowns()
    }
}