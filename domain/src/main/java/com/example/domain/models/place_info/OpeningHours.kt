package com.example.domain.models.place_info

data class OpeningHours(
    val openNow: Boolean,
    val periods: List<Period>,
    val weekdayText: List<String>
)