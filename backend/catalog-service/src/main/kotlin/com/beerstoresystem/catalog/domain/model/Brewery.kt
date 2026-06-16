package com.beerstoresystem.catalog.domain.model

data class Brewery(
    val id: Long,
    val name: String,
    val websiteUrl: String?,
    val foundedYear: Int?,
    val country: Country?
)
