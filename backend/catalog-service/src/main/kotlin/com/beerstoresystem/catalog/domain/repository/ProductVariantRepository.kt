package com.beerstoresystem.catalog.domain.repository

import com.beerstoresystem.catalog.domain.model.ProductVariant

interface ProductVariantRepository {
    fun findAllByBeerIdWithDetails(beerId: Long): List<ProductVariant>
    fun findAllByIdInWithDetails(ids: List<Long>): List<ProductVariant>
    fun findByIdWithDetails(id: Long): ProductVariant?
    fun findByIdOrNull(id: Long): ProductVariant?
}
