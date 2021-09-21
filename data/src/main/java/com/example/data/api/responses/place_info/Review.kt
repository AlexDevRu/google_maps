package com.example.data.api.responses.place_info

data class Review(
    val author_name: String,
    val author_url: String? = null,
    val language: String,
    val profile_photo_url: String? = null,
    val rating: Int,
    val relative_time_description: String,
    val text: String? = null,
    val time: Int
)