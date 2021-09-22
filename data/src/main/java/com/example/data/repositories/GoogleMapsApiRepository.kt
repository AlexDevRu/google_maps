package com.example.data.repositories

import com.example.data.api.GoogleMapApiService
import com.example.data.mappers.toModel
import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import com.example.domain.repositories.IGoogleMapApiRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.example.domain.common.DIRECTION_TYPE


class GoogleMapsApiRepository (
    private val service: GoogleMapApiService
): IGoogleMapApiRepository {

    private val DEFAULT_LANGUAGE = "en"

    private val directionTypesMap = mapOf(
        DIRECTION_TYPE.DRIVING to "driving",
        DIRECTION_TYPE.WALKING to "walking",
        DIRECTION_TYPE.BICYCLING to "bicycling",
        DIRECTION_TYPE.TRANSIT to "transit",
    )

    override fun getInfoByLocation(placeId: String, language: String?): Single<PlaceInfo> {
        return service.getPlaceInfo(placeId, language ?: DEFAULT_LANGUAGE).subscribeOn(Schedulers.io()).map {
            it.toModel()
        }
    }

    override fun getDirection(origin: Location, destination: Location, directionType: DIRECTION_TYPE, language: String?): Single<Direction> {
        val originStr = "${origin.lat},${origin.lng}"
        val destStr = "${destination.lat},${destination.lng}"
        return service.getDirection(originStr, destStr, directionTypesMap[directionType]!!, language ?: DEFAULT_LANGUAGE)
            .subscribeOn(Schedulers.io()).map { it.toModel() }
    }
}