package com.beerstoresystem.warehouse.domain.repository

import com.beerstoresystem.warehouse.domain.model.PickupPoint

interface PickupPointDomainRepository {
    fun findAll(): List<PickupPoint>
    fun findByIsActive(isActive: Boolean): List<PickupPoint>
}
