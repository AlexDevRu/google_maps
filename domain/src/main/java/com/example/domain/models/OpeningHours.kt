package com.example.domain.models

data class OpeningHours(
    val openNow: Boolean,
    val periods: List<Period>,
    val weekdayText: List<String>
)