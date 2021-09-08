package com.example.domain.repositories

import com.example.domain.models.PlaceInfo
import io.reactivex.Single

interface IGoogleMapApiRepository {
    fun getInfoByLocation(placeId: String): Single<PlaceInfo>
}