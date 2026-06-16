package com.beerstoresystem.catalog.infrastructure.persistence.repository

import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.repository.BreweryRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BreweryJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.mapper.toDomain
import org.springframework.stereotype.Component

@Component
class BreweryRepositoryAdapter(
    private val jpaRepository: BreweryJpaRepository
) : BreweryRepository {

    override fun findAll(): List<Brewery> =
        jpaRepository.findAll().map { it.toDomain() }
}
