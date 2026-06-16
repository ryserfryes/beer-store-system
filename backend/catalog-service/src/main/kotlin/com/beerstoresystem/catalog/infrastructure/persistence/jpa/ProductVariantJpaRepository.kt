package com.beerstoresystem.catalog.infrastructure.persistence.jpa

import com.beerstoresystem.catalog.infrastructure.persistence.entity.ProductVariantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductVariantJpaRepository : JpaRepository<ProductVariantEntity, Long> {

    @Query(
        "SELECT pv FROM ProductVariantEntity pv " +
        "JOIN FETCH pv.beer b " +
        "JOIN FETCH b.brewery " +
        "LEFT JOIN FETCH pv.packageType " +
        "WHERE pv.beer.id = :beerId"
    )
    fun findAllByBeerIdWithDetails(beerId: Long): List<ProductVariantEntity>

    @Query(
        "SELECT pv FROM ProductVariantEntity pv " +
        "JOIN FETCH pv.beer b " +
        "JOIN FETCH b.brewery " +
        "LEFT JOIN FETCH pv.packageType " +
        "WHERE pv.id IN :ids"
    )
    fun findAllByIdInWithDetails(ids: List<Long>): List<ProductVariantEntity>

    @Query(
        "SELECT pv FROM ProductVariantEntity pv " +
        "JOIN FETCH pv.beer b " +
        "JOIN FETCH b.brewery " +
        "LEFT JOIN FETCH pv.packageType " +
        "WHERE pv.id = :id"
    )
    fun findByIdWithDetails(id: Long): ProductVariantEntity?
}
