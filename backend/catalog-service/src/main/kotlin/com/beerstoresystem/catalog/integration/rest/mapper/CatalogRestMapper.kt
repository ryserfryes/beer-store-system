package com.beerstoresystem.catalog.integration.rest.mapper

import com.beerstoresystem.catalog.domain.model.Beer
import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.model.BeerWithVariants
import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.model.CatalogPage
import com.beerstoresystem.catalog.domain.model.ProductVariant
import com.beerstoresystem.catalog.integration.rest.dto.BeerDetailDto
import com.beerstoresystem.catalog.integration.rest.dto.BeerStyleDto
import com.beerstoresystem.catalog.integration.rest.dto.BeerSummaryDto
import com.beerstoresystem.catalog.integration.rest.dto.BreweryDto
import com.beerstoresystem.catalog.integration.rest.dto.CatalogPageDto
import com.beerstoresystem.catalog.integration.rest.dto.VariantDto

fun BeerStyle.toDto() = BeerStyleDto(
    id = id,
    name = name,
    description = description
)

fun Brewery.toDto() = BreweryDto(
    id = id,
    name = name,
    websiteUrl = websiteUrl,
    foundedYear = foundedYear,
    country = country?.name
)

fun ProductVariant.toDto() = VariantDto(
    id = id,
    sku = sku,
    volumeMl = volumeMl,
    unitPrice = unitPrice,
    isActive = isActive,
    packageType = packageType?.name
)

fun BeerWithVariants.toSummaryDto() = BeerSummaryDto(
    id = beer.id,
    name = beer.name,
    brewery = beer.brewery?.name ?: "",
    style = beer.style?.name ?: "",
    abv = beer.abv,
    variants = variants.map { it.toDto() }
)

fun Beer.toDetailDto(variants: List<ProductVariant>) = BeerDetailDto(
    id = id,
    name = name,
    description = description,
    abv = abv,
    isActive = isActive,
    brewery = brewery?.toDto() ?: BreweryDto(0, "", null, null, null),
    style = style?.toDto() ?: BeerStyleDto(0, "", null),
    variants = variants.map { it.toDto() }
)

fun CatalogPage.toDto() = CatalogPageDto(
    content = content.map { it.toSummaryDto() },
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)
