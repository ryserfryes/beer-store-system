package com.beerstoresystem.catalog.domain.repository

import com.beerstoresystem.catalog.domain.model.Beer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BeerRepository {
    fun findAllActiveWithDetails(pageable: Pageable): Page<Beer>
    fun findByIdWithDetails(id: Long): Beer?
}
