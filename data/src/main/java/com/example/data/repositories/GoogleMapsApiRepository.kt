package com.example.data.repositories

import com.example.data.api.GoogleMapApiService
import com.example.data.mappers.toModel
import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import com.example.domain.repositories.IGoogleMapApiRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import android.util.Log


class GoogleMapsApiRepository @Inject constructor(
    private val service: GoogleMapApiService
): IGoogleMapApiRepository {

    override fun getInfoByLocation(placeId: String): Single<PlaceInfo> {
        return service.getPlaceInfo(placeId).subscribeOn(Schedulers.io()).map {
            Log.e("MapsActivity", it.toString())
            return@map it.toModel()
        }
    }

    override fun getDirection(origin: Location, destination: Location): Single<Direction> {
        val originStr = "${origin.lat},${origin.lng}"
        val destStr = "${destination.lat},${destination.lng}"
        return service.getDirection(originStr, destStr)
            .subscribeOn(Schedulers.io()).map { it.toModel() }
    }
}