package com.example.maps.ui.fragments.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.domain.models.place_info.PlaceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.domain.common.Result
import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.maps.utils.GoogleMapUtil
import com.google.android.libraries.places.api.model.Place
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(app: Application, private val googleMapsApiRepository: GoogleMapsApiRepository): AndroidViewModel(app) {

    private val compositeDisposable = CompositeDisposable()

    private val _placeData = MutableLiveData<Result<PlaceInfo>>()
    val placeData: LiveData<Result<PlaceInfo>> = _placeData

    private val _direction = MutableLiveData<Result<Direction>>()
    val direction: LiveData<Result<Direction>> = _direction

    private var placeInfo: Single<PlaceInfo>? = null
    private var placeInfoSubscriber: Disposable? = null

    var currentPlaceId: String? = null
        private set


    val googleMapUtil = GoogleMapUtil(app)

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
        compositeDisposable.add(placeInfoSubscriber!!)
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

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}