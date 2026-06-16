package com.beerstoresystem.catalog.infrastructure.persistence.repository

import com.beerstoresystem.catalog.domain.model.Beer
import com.beerstoresystem.catalog.domain.repository.BeerRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BeerJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.mapper.toDomain
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class BeerRepositoryAdapter(
    private val jpaRepository: BeerJpaRepository
) : BeerRepository {

    override fun findAllActiveWithDetails(pageable: Pageable): Page<Beer> =
        jpaRepository.findAllActiveWithDetails(pageable).map { it.toDomain() }

    override fun findByIdWithDetails(id: Long): Beer? =
        jpaRepository.findByIdWithDetails(id)?.toDomain()
}
