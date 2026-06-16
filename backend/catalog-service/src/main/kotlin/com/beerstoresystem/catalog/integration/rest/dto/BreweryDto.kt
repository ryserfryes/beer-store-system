package com.beerstoresystem.catalog.integration.rest.dto

data class BreweryDto(
    val id: Long,
    val name: String,
    val websiteUrl: String?,
    val foundedYear: Int?,
    val country: String?
)
