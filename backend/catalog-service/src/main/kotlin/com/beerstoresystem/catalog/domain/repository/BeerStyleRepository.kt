package com.beerstoresystem.catalog.domain.repository

import com.beerstoresystem.catalog.domain.model.BeerStyle

interface BeerStyleRepository {
    fun findAll(): List<BeerStyle>
}
