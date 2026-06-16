package com.beerstoresystem.catalog.domain.repository

import com.beerstoresystem.catalog.domain.model.ProductView

interface ProductViewRepository {
    fun save(productView: ProductView): ProductView
    fun count(): Long
}
