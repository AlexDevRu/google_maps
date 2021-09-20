package com.example.domain.repositories

import com.example.domain.common.DIRECTION_TYPE
import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import io.reactivex.Single

interface IGoogleMapApiRepository {
    fun getInfoByLocation(placeId: String, language: String?): Single<PlaceInfo>
    fun getDirection(origin: Location, destination: Location, directionType: DIRECTION_TYPE, language: String?): Single<Direction>
}