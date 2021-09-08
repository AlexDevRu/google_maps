package com.example.data.repositories

import com.example.data.api.GoogleMapApiService
import com.example.data.mappers.toModel
import com.example.domain.models.PlaceInfo
import com.example.domain.repositories.IGoogleMapApiRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class GoogleMapsApiRepository @Inject constructor(
    private val service: GoogleMapApiService
): IGoogleMapApiRepository {

    override fun getInfoByLocation(placeId: String): Single<PlaceInfo> {
        return service.getPlaceInfo(placeId).subscribeOn(Schedulers.io()).map {
            it.toModel()
        }
    }
}