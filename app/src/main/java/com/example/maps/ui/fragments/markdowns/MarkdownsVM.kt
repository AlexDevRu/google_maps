package com.example.maps.ui.fragments.markdowns

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.common.Result
import com.example.domain.models.Markdown
import com.example.domain.repositories.IMarkdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class MarkdownsVM @Inject constructor(
    private val repository: IMarkdownRepository
): ViewModel() {

    private val _markdowns = MutableLiveData<Result<List<Markdown>>>()
    val markdowns: LiveData<Result<List<Markdown>>> = _markdowns

    private val compositeDisposable = CompositeDisposable()

    fun getMarkdowns() {
        _markdowns.value = Result.Loading()
        compositeDisposable.add(
            repository.getMarkdowns().observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _markdowns.value = Result.Success(it)
                }, {
                    _markdowns.value = Result.Failure(it)
                })
        )
    }

    fun deleteMarkdown(markdown: Markdown) {
        repository.deleteMarkdownById(markdown.id)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}