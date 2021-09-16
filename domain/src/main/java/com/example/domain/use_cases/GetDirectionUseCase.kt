package com.example.domain.use_cases

import com.example.domain.models.Location
import com.example.domain.models.directions.Direction
import com.example.domain.repositories.IGoogleMapApiRepository
import io.reactivex.Single

class GetDirectionUseCase(private val googleMapApiRepository: IGoogleMapApiRepository) {
    fun invoke(origin: Location, destination: Location): Single<Direction> {
        return googleMapApiRepository.getDirection(origin, destination)
    }
}