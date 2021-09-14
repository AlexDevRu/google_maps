package com.example.data.api.responses.place_info

import com.example.data.api.responses.Location

data class Viewport(
    val northeast: Location? = null,
    val southwest: Location? = null
)