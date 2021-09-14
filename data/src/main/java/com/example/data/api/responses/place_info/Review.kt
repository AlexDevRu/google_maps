package com.example.data.api.responses.place_info

data class Review(
    val author_name: String? = null,
    val author_url: String? = null,
    val language: String? = null,
    val profile_photo_url: String? = null,
    val rating: Int? = null,
    val relative_time_description: String? = null,
    val text: String? = null,
    val time: Int? = null
)