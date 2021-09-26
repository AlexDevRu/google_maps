package com.example.maps.ui.fragments.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.domain.exceptions.EmptyResultException
import com.example.domain.models.Markdown
import com.example.domain.use_cases.markdowns.DeleteMarkdownByIdUseCase
import com.example.domain.use_cases.markdowns.InsertMarkdownUseCase
import com.example.domain.use_cases.markdowns.IsPlaceInMarkdownsUseCase
import com.example.googlemaps.models.DirectionSegmentUI
import com.example.googlemaputil_core.common.DIRECTION_TYPE
import com.example.googlemaputil_core.common.Result
import com.example.googlemaputil_core.models.place_info.PlaceInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class MainVM(
    app: Application,
    private val isPlaceInMarkdownsUseCase: IsPlaceInMarkdownsUseCase,
    private val insertMarkdownUseCase: InsertMarkdownUseCase,
    private val deleteMarkdownByIdUseCase: DeleteMarkdownByIdUseCase
): AndroidViewModel(app) {

    companion object {
        private const val TAG = "MainVM"
    }

    var directionType = DIRECTION_TYPE.DRIVING
    var currentCountryCode: String? = null

    var directionsSegments: List<DirectionSegmentUI>? = null

    private val compositeDisposable = CompositeDisposable()

    var currentPlaceId: String? = null
        private set


    val placeInfo = BehaviorSubject.create<Result<PlaceInfo>>()
    val currentPlaceFavorite = BehaviorSubject.create<Result<Boolean>>()


    fun findPlaceInMarkdowns(placeId: String) {
        currentPlaceId = placeId

        currentPlaceFavorite.onNext(Result.Loading())

        val placeInMarkdown = isPlaceInMarkdownsUseCase.invoke(placeId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                currentPlaceFavorite.onNext(Result.Success(true))
            }, {
                Log.e(TAG, "place markdowns error ${it.message}")
                if(it is EmptyResultException)
                    currentPlaceFavorite.onNext(Result.Success(false))
                else
                    currentPlaceFavorite.onNext(Result.Failure(it))
            })

        compositeDisposable.add(placeInMarkdown)
    }

    fun toggleFavoriteCurrentPlace() {
        if(currentPlaceId == null)
            return

        currentPlaceFavorite.onNext(Result.Loading())

        val subscriber = isPlaceInMarkdownsUseCase.invoke(currentPlaceId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "update favorite place success")
                val deleteSubscriber = deleteMarkdownByIdUseCase.invoke(currentPlaceId!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        currentPlaceFavorite.onNext(Result.Success(false))
                    }, {
                        currentPlaceFavorite.onNext(Result.Failure(it))
                    })
                compositeDisposable.add(deleteSubscriber)
            }, {
                Log.e(TAG, "update favorite place exception ${it.message}")
                if(it is EmptyResultException) {

                    val _placeInfo = (placeInfo.value as Result.Success).value

                    val markdown = Markdown(
                        _placeInfo.placeId!!,
                        _placeInfo.name,
                        _placeInfo.address,
                        _placeInfo.location
                    )
                    val insertSubscriber = insertMarkdownUseCase.invoke(markdown)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            currentPlaceFavorite.onNext(Result.Success(true))
                        }, {
                            currentPlaceFavorite.onNext(Result.Failure(it))
                        })
                    compositeDisposable.add(insertSubscriber)
                }
            })

        compositeDisposable.add(subscriber)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}