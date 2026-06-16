package com.beerstoresystem.catalog.application

import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.model.BeerWithVariants
import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.model.CatalogPage
import com.beerstoresystem.catalog.domain.model.ProductVariant

interface CatalogUseCase {
    fun getCatalog(page: Int, size: Int): CatalogPage
    fun getBeerDetail(id: Long): BeerWithVariants
    fun getVariantById(id: Long): ProductVariant
    fun getAllStyles(): List<BeerStyle>
    fun getAllBreweries(): List<Brewery>
    fun getVariantsByIds(ids: List<Long>): List<ProductVariant>
    fun recordView(variantId: Long, customerId: Long?)
}
