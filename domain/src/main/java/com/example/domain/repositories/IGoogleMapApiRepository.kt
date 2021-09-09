package com.example.domain.repositories

import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import io.reactivex.Single

interface IGoogleMapApiRepository {
    fun getInfoByLocation(placeId: String): Single<PlaceInfo>
    fun getDirection(origin: Location, destination: Location): Single<Direction>
}