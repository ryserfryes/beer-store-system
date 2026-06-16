package com.beerstoresystem.catalog.domain.repository

import com.beerstoresystem.catalog.domain.model.Brewery

interface BreweryRepository {
    fun findAll(): List<Brewery>
}
