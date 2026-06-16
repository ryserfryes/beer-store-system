package com.beerstoresystem.catalog.infrastructure.persistence.repository

import com.beerstoresystem.catalog.domain.model.ProductView
import com.beerstoresystem.catalog.domain.repository.ProductViewRepository
import com.beerstoresystem.catalog.infrastructure.persistence.entity.ProductViewEntity
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.ProductVariantJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.ProductViewJpaRepository
import org.springframework.stereotype.Component

@Component
class ProductViewRepositoryAdapter(
    private val jpaRepository: ProductViewJpaRepository,
    private val variantJpaRepository: ProductVariantJpaRepository
) : ProductViewRepository {

    override fun save(productView: ProductView): ProductView {
        val variantEntity = variantJpaRepository.findById(productView.variantId).orElseThrow {
            NoSuchElementException("Variant not found: ${productView.variantId}")
        }
        val entity = ProductViewEntity().apply {
            variant = variantEntity
            customerId = productView.customerId
            viewedAt = productView.viewedAt
        }
        val saved = jpaRepository.save(entity)
        return ProductView(
            id = saved.id,
            variantId = saved.variant!!.id,
            customerId = saved.customerId,
            viewedAt = saved.viewedAt
        )
    }

    override fun count(): Long = jpaRepository.count()
}
