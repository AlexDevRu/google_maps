package com.example.domain.models

import java.util.*

data class Markdown(
    val id: UUID = UUID.randomUUID(),
    val name: String? = null,
    val address: String? = null,
    val location: Location? = null
)