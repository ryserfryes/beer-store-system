package com.beerstoresystem.catalog.infrastructure.persistence.repository

import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.repository.BeerStyleRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BeerStyleJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.mapper.toDomain
import org.springframework.stereotype.Component

@Component
class BeerStyleRepositoryAdapter(
    private val jpaRepository: BeerStyleJpaRepository
) : BeerStyleRepository {

    override fun findAll(): List<BeerStyle> =
        jpaRepository.findAll().map { it.toDomain() }
}
