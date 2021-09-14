package com.example.data.api.responses.place_info

data class OpeningHours(
    val open_now: Boolean,
    val periods: List<Period>? = null,
    val weekday_text: List<String>? = null
)