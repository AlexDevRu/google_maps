package com.example.maps.ui.fragments.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain.common.Result
import com.example.domain.exceptions.EmptyResultException
import com.example.domain.models.Location
import com.example.domain.models.Markdown
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import com.example.domain.use_cases.GetDirectionUseCase
import com.example.domain.use_cases.GetInfoByLocationUseCase
import com.example.domain.use_cases.markdowns.DeleteMarkdownByIdUseCase
import com.example.domain.use_cases.markdowns.InsertMarkdownUseCase
import com.example.domain.use_cases.markdowns.IsPlaceInMarkdownsUseCase
import com.example.maps.utils.GoogleMapUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    app: Application,
    private val getInfoByLocationUseCase: GetInfoByLocationUseCase,
    private val getDirectionUseCase: GetDirectionUseCase,
    private val isPlaceInMarkdownsUseCase: IsPlaceInMarkdownsUseCase,
    private val insertMarkdownUseCase: InsertMarkdownUseCase,
    private val deleteMarkdownByIdUseCase: DeleteMarkdownByIdUseCase
): AndroidViewModel(app) {

    companion object {
        private const val TAG = "MainVM"
    }

    val googleMapUtil = GoogleMapUtil(app)

    private val compositeDisposable = CompositeDisposable()

    private val _placeData = MutableLiveData<Result<PlaceInfo>>()
    val placeData: LiveData<Result<PlaceInfo>> = _placeData

    private val _direction = MutableLiveData<Result<Direction>>()
    val direction: LiveData<Result<Direction>> = _direction


    private val _currentMapMode = MutableLiveData(googleMapUtil.markerMode)
    val currentMapMode: LiveData<GoogleMapUtil.MAP_MODE> = _currentMapMode

    private val _currentPlaceFavorite = MutableLiveData(false)
    val currentPlaceFavorite: LiveData<Boolean> = _currentPlaceFavorite


    private var placeInfo: Single<PlaceInfo>? = null
    private var placeInfoSubscriber: Disposable? = null

    var currentPlaceId: String? = null
        private set


    fun toggleMapMode() {
        googleMapUtil.markerMode = if(googleMapUtil.markerMode == GoogleMapUtil.MAP_MODE.PLACE)
            GoogleMapUtil.MAP_MODE.DIRECTION
        else
            GoogleMapUtil.MAP_MODE.PLACE

        _currentMapMode.value = googleMapUtil.markerMode
    }


    fun getInfoByLocation(placeId: String) {
        currentPlaceId = placeId

        _placeData.value = Result.Loading()
        placeInfo = getInfoByLocationUseCase.invoke(placeId)

        placeInfoSubscriber?.dispose()
        placeInfoSubscriber = placeInfo!!.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _placeData.value = Result.Success(it)
            }, {
                _placeData.value = Result.Failure(it)
            })


        val placeInMarkdown = isPlaceInMarkdownsUseCase.invoke(currentPlaceId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _currentPlaceFavorite.value = true
            }, {
                Log.e(TAG, "place markdowns error ${it.message}")
                if(it is EmptyResultException)
                    _currentPlaceFavorite.value = false
            })


        compositeDisposable.add(placeInfoSubscriber!!)
        compositeDisposable.add(placeInMarkdown)
    }



    fun retry() {
        if(currentPlaceId != null)
            getInfoByLocation(currentPlaceId!!)
    }

    fun getDirection(origin: Location, destination: Location) {
        _direction.value = Result.Loading()
        compositeDisposable.add(
            getDirectionUseCase.invoke(origin, destination)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _direction.value = Result.Success(it)
                }, {
                    _direction.value = Result.Failure(it)
                })
        )
    }

    fun setPlace(place: Place) {
        setPlace(place.id!!, place.latLng!!)
    }

    fun setPlace(markdown: Markdown) {
        if(markdown.location == null) return

        val latLng = LatLng(markdown.location!!.lat, markdown.location!!.lng)
        setPlace(markdown.placeId, latLng)
    }

    private fun setPlace(placeId: String, latLng: LatLng) {
        when(googleMapUtil.markerMode) {
            GoogleMapUtil.MAP_MODE.PLACE -> {
                googleMapUtil.createPlaceMarker(latLng)
                getInfoByLocation(placeId)
            }
            GoogleMapUtil.MAP_MODE.DIRECTION -> {
                if(googleMapUtil.currentDirectionMarker == GoogleMapUtil.DIRECTION_MARKER.ORIGIN)
                    googleMapUtil.createOriginMarker(latLng)
                else
                    googleMapUtil.createDestinationMarker(latLng)
            }
        }
        googleMapUtil.moveCamera(latLng)
    }

    fun toggleFavoriteCurrentPlace() {
        if(currentPlaceId == null)
            return

        val subscriber = isPlaceInMarkdownsUseCase.invoke(currentPlaceId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "update favorite place success")
                val deleteSubscriber = deleteMarkdownByIdUseCase.invoke(currentPlaceId!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _currentPlaceFavorite.value = false
                    }, {})
                compositeDisposable.add(deleteSubscriber)
            }, {
                Log.e(TAG, "update favorite place exception ${it.message}")
                if(it is EmptyResultException) {
                    val placeInfo = (_placeData.value as Result.Success).value
                    val markdown = Markdown(currentPlaceId!!, placeInfo.name, placeInfo.address, placeInfo.location)
                    val insertSubscriber = insertMarkdownUseCase.invoke(markdown)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            _currentPlaceFavorite.value = true
                        }, {})
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