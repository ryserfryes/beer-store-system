package com.beerstoresystem.catalog.infrastructure.persistence.jpa

import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerStyleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BeerStyleJpaRepository : JpaRepository<BeerStyleEntity, Long>
