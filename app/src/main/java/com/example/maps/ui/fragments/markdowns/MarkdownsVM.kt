package com.example.maps.ui.fragments.markdowns

import android.util.Log
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

    private val _markdowns = MutableLiveData<Result<MutableList<Markdown>>>()
    val markdowns: LiveData<Result<MutableList<Markdown>>> = _markdowns

    private val compositeDisposable = CompositeDisposable()

    fun getMarkdowns() {
        _markdowns.value = Result.Loading()
        compositeDisposable.add(
            repository.getMarkdowns().observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _markdowns.value = Result.Success(it.toMutableList())
                }, {
                    _markdowns.value = Result.Failure(it)
                })
        )
    }

    fun deleteMarkdown(markdown: Markdown) {
        compositeDisposable.add(
            repository.deleteMarkdownById(markdown.placeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.e("MapsActivity", "delete successfully")
                }, {
                    Log.e("MapsActivity", "exception delete ${it.message}")
                })
        )
        (_markdowns.value as Result.Success).value.remove(markdown)

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}