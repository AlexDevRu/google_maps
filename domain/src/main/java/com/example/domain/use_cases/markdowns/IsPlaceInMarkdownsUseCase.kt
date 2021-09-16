package com.example.domain.use_cases.markdowns

import com.example.domain.models.Markdown
import com.example.domain.repositories.IFirebaseRepository
import com.example.domain.repositories.IMarkdownRepository
import io.reactivex.Maybe
import io.reactivex.Single

class IsPlaceInMarkdownsUseCase(
    private val markdownRepository: IMarkdownRepository,
    private val firebaseRepository: IFirebaseRepository
) {
    fun invoke(id: String): Single<Markdown> {
        return firebaseRepository.isPlaceInMarkdowns(id)//markdownRepository.isPlaceInMarkdowns(id)
    }
}