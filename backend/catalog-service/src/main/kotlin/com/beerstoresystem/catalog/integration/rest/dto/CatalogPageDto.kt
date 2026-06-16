package com.beerstoresystem.catalog.integration.rest.dto

data class CatalogPageDto(
    val content: List<BeerSummaryDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
