package com.example.maps.ui.fragments.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.domain.common.Result
import com.example.domain.models.Location
import com.example.domain.models.Markdown
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import com.example.domain.repositories.IMarkdownRepository
import com.example.maps.utils.GoogleMapUtil
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    app: Application,
    private val googleMapsApiRepository: GoogleMapsApiRepository,
    private val markdownRepository: IMarkdownRepository
): AndroidViewModel(app) {

    val googleMapUtil = GoogleMapUtil(app)

    private val compositeDisposable = CompositeDisposable()

    private val _placeData = MutableLiveData<Result<PlaceInfo>>()
    val placeData: LiveData<Result<PlaceInfo>> = _placeData

    private val _direction = MutableLiveData<Result<Direction>>()
    val direction: LiveData<Result<Direction>> = _direction


    private val _currentMapMode = MutableLiveData(googleMapUtil.markerMode)
    val currentMapMode: LiveData<GoogleMapUtil.MAP_MODE> = _currentMapMode

    private val _currentPlaceFavorite = MutableLiveData<Boolean>()
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
        placeInfo = googleMapsApiRepository.getInfoByLocation(placeId)

        placeInfoSubscriber?.dispose()
        placeInfoSubscriber = placeInfo!!.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _placeData.value = Result.Success(it)
            }, {
                _placeData.value = Result.Failure(it)
            })


        val placeInMarkdown = markdownRepository.isPlaceInMarkdowns(currentPlaceId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _currentPlaceFavorite.value = true
            }, {
                Log.e("MapsActivity", "place markdowns error ${it.message}")
            }, {
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
            googleMapsApiRepository.getDirection(origin, destination)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _direction.value = Result.Success(it)
                }, {
                    _direction.value = Result.Failure(it)
                })
        )
    }

    fun setPlace(place: Place) {
        when(googleMapUtil.markerMode) {
            GoogleMapUtil.MAP_MODE.PLACE -> {
                googleMapUtil.createPlaceMarker(place.latLng!!)
                getInfoByLocation(place.id!!)
            }
            GoogleMapUtil.MAP_MODE.DIRECTION -> {
                if(googleMapUtil.currentDirectionMarker == GoogleMapUtil.DIRECTION_MARKER.ORIGIN)
                    googleMapUtil.createOriginMarker(place.latLng!!)
                else
                    googleMapUtil.createDestinationMarker(place.latLng!!)
            }
        }
        googleMapUtil.moveCamera(place.latLng!!)
    }

    fun toggleFavoriteCurrentPlace() {
        if(currentPlaceId == null)
            return

        val subscriber = markdownRepository.isPlaceInMarkdowns(currentPlaceId!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.e("MapsActivity", "update success")
                val deleteSubscriber = markdownRepository.deleteMarkdownById(currentPlaceId!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _currentPlaceFavorite.value = false
                    }, {

                    })
                compositeDisposable.add(deleteSubscriber)
            }, {
                Log.e("MapsActivity", "update exception ${it.message}")
            }, {
                Log.e("MapsActivity", "update complete")
                val placeInfo = (_placeData.value as Result.Success).value
                val markdown = Markdown(currentPlaceId!!, placeInfo.name, placeInfo.address, placeInfo.location)
                val insertSubscriber = markdownRepository.insert(markdown)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _currentPlaceFavorite.value = true
                    }, {

                    })
                compositeDisposable.add(insertSubscriber)
            })

        compositeDisposable.add(subscriber)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}