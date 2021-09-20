package com.example.data.api

import com.example.data.api.ApiConstants.API_KEY
import com.example.data.api.responses.directions.DirectionResponse
import com.example.data.api.responses.place_info.PlaceInfoResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapApiService {

    @GET("place/details/json")
    fun getPlaceInfo(
        @Query("place_id") placeId: String,
        @Query("language") lang: String,
        @Query("key") key: String = API_KEY
    ): Single<PlaceInfoResponse>

    @GET("directions/json")
    fun getDirection(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") directionType: String,
        @Query("language") lang: String,
        @Query("key") key: String = API_KEY
    ): Single<DirectionResponse>
}