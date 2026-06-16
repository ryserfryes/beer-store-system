package com.beerstoresystem.catalog.infrastructure.persistence.jpa

import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BeerJpaRepository : JpaRepository<BeerEntity, Long> {

    @Query(
        value = "SELECT DISTINCT b FROM BeerEntity b " +
                "JOIN FETCH b.brewery " +
                "JOIN FETCH b.style " +
                "WHERE b.isActive = true",
        countQuery = "SELECT COUNT(DISTINCT b) FROM BeerEntity b WHERE b.isActive = true"
    )
    fun findAllActiveWithDetails(pageable: Pageable): Page<BeerEntity>

    @Query(
        "SELECT b FROM BeerEntity b " +
        "JOIN FETCH b.brewery " +
        "JOIN FETCH b.style " +
        "WHERE b.id = :id"
    )
    fun findByIdWithDetails(id: Long): BeerEntity?
}
