package com.example.data.api

import com.example.data.api.ApiConstants.API_KEY
import com.example.data.api.responses.PlaceInfoResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapApiService {

    @GET("place/details/json")
    fun getPlaceInfo(
        @Query("place_id") placeId: String,
        @Query("key") key: String = API_KEY
    ): Single<PlaceInfoResponse>
}