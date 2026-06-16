package com.beerstoresystem.supply.integration.rest.mapper

import com.beerstoresystem.supply.domain.model.Supplier
import com.beerstoresystem.supply.integration.rest.dto.SupplierDto

fun Supplier.toDto() = SupplierDto(
    id = id,
    name = name,
    countryId = countryId,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    createdAt = createdAt
)
