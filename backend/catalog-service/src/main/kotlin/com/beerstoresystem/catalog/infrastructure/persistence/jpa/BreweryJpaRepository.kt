package com.beerstoresystem.catalog.infrastructure.persistence.jpa

import com.beerstoresystem.catalog.infrastructure.persistence.entity.BreweryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BreweryJpaRepository : JpaRepository<BreweryEntity, Long>
