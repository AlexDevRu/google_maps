package com.example.maps.ui.fragments.markdowns

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.common.Result
import com.example.domain.models.Markdown
import com.example.domain.use_cases.markdowns.DeleteMarkdownByIdUseCase
import com.example.domain.use_cases.markdowns.GetMarkdownsUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MarkdownsVM (
    private val getMarkdownsUseCase: GetMarkdownsUseCase,
    private val deleteMarkdownByIdUseCase: DeleteMarkdownByIdUseCase
): ViewModel() {

    companion object {
        private const val TAG = "MarkdownsVM"
    }

    private val _markdowns = MutableLiveData<Result<MutableList<Markdown>>>()
    val markdowns: LiveData<Result<MutableList<Markdown>>> = _markdowns

    private val compositeDisposable = CompositeDisposable()

    fun getMarkdowns() {
        _markdowns.value = Result.Loading()
        compositeDisposable.add(
            getMarkdownsUseCase.invoke().observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _markdowns.value = Result.Success(it.toMutableList())
                }, {
                    _markdowns.value = Result.Failure(it)
                })
        )
    }

    fun deleteMarkdown(markdown: Markdown) {
        compositeDisposable.add(
            deleteMarkdownByIdUseCase.invoke(markdown.placeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "delete markdown successfully")
                }, {
                    Log.e(TAG, "exception delete markdown ${it.message}")
                })
        )
        (_markdowns.value as Result.Success).value.remove(markdown)

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}