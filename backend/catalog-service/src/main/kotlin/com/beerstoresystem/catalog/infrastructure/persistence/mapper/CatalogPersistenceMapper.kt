package com.beerstoresystem.catalog.infrastructure.persistence.mapper

import com.beerstoresystem.catalog.domain.model.Beer
import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.model.Country
import com.beerstoresystem.catalog.domain.model.PackageType
import com.beerstoresystem.catalog.domain.model.ProductVariant
import com.beerstoresystem.catalog.domain.model.ProductView
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerStyleEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BreweryEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.CountryEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.PackageTypeEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.ProductVariantEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.ProductViewEntity

fun CountryEntity.toDomain() = Country(
    id = id,
    isoCode = isoCode,
    name = name
)

fun BeerStyleEntity.toDomain() = BeerStyle(
    id = id,
    name = name,
    description = description
)

fun BreweryEntity.toDomain() = Brewery(
    id = id,
    name = name,
    websiteUrl = websiteUrl,
    foundedYear = foundedYear,
    country = country?.toDomain()
)

fun PackageTypeEntity.toDomain() = PackageType(
    id = id,
    code = code,
    name = name
)

fun BeerEntity.toDomain() = Beer(
    id = id,
    name = name,
    description = description,
    abv = abv,
    isActive = isActive,
    brewery = brewery?.toDomain(),
    style = style?.toDomain(),
    createdAt = createdAt
)

fun ProductVariantEntity.toDomain() = ProductVariant(
    id = id,
    sku = sku,
    volumeMl = volumeMl,
    unitPrice = unitPrice,
    isActive = isActive,
    beer = beer?.toDomain(),
    packageType = packageType?.toDomain(),
    createdAt = createdAt
)

fun ProductView.toEntity(variantEntity: ProductVariantEntity): ProductViewEntity =
    ProductViewEntity().apply {
        variant = variantEntity
        customerId = this@toEntity.customerId
        viewedAt = this@toEntity.viewedAt
    }
