package com.example.maps.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.repositories.GoogleMapsApiRepository
import com.example.domain.models.place_info.PlaceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.domain.common.Result
import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(private val googleMapsApiRepository: GoogleMapsApiRepository): ViewModel() {

    private var job: Job? = null

    private val compositeDisposable = CompositeDisposable()

    private val _placeData = MutableLiveData<Result<PlaceInfo>>()
    val placeData: LiveData<Result<PlaceInfo>> = _placeData

    private val _direction = MutableLiveData<Result<Direction>>()
    val direction: LiveData<Result<Direction>> = _direction

    fun getInfoByLocation(placeId: String) {
        _placeData.value = Result.Loading()
        compositeDisposable.add(
            googleMapsApiRepository.getInfoByLocation(placeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _placeData.value = Result.Success(it)
                }, {
                    _placeData.value = Result.Failure(it)
                })
        )
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

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}