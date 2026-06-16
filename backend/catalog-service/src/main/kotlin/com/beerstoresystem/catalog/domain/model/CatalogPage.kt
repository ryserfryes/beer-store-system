package com.beerstoresystem.catalog.domain.model

data class BeerWithVariants(
    val beer: Beer,
    val variants: List<ProductVariant>
)

data class CatalogPage(
    val content: List<BeerWithVariants>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
