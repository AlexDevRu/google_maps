package com.example.data.api.responses.place_info

data class PlaceInfoResponse(
    val html_attributions: List<Any>,
    val result: Result,
    val status: String
)