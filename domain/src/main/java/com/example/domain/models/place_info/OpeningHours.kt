package com.example.domain.models.place_info

data class OpeningHours(
    val openNow: Boolean = false,
    val periods: List<Period>? = null,
    val weekdayText: List<String>? = null
)