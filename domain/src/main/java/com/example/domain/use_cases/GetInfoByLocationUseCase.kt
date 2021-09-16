package com.example.domain.use_cases

import com.example.domain.models.place_info.PlaceInfo
import com.example.domain.repositories.IGoogleMapApiRepository
import io.reactivex.Single

class GetInfoByLocationUseCase(private val googleMapApiRepository: IGoogleMapApiRepository) {
    fun invoke(placeId: String): Single<PlaceInfo> {
        return googleMapApiRepository.getInfoByLocation(placeId)
    }
}