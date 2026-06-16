package com.beerstoresystem.catalog.infrastructure.persistence.repository

import com.beerstoresystem.catalog.domain.model.ProductVariant
import com.beerstoresystem.catalog.domain.repository.ProductVariantRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.ProductVariantJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.mapper.toDomain
import org.springframework.stereotype.Component

@Component
class ProductVariantRepositoryAdapter(
    private val jpaRepository: ProductVariantJpaRepository
) : ProductVariantRepository {

    override fun findAllByBeerIdWithDetails(beerId: Long): List<ProductVariant> =
        jpaRepository.findAllByBeerIdWithDetails(beerId).map { it.toDomain() }

    override fun findAllByIdInWithDetails(ids: List<Long>): List<ProductVariant> =
        jpaRepository.findAllByIdInWithDetails(ids).map { it.toDomain() }

    override fun findByIdWithDetails(id: Long): ProductVariant? =
        jpaRepository.findByIdWithDetails(id)?.toDomain()

    override fun findByIdOrNull(id: Long): ProductVariant? =
        jpaRepository.findById(id).orElse(null)?.toDomain()
}
