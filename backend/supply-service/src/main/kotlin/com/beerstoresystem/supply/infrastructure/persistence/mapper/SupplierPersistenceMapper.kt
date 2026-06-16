package com.beerstoresystem.supply.infrastructure.persistence.mapper

import com.beerstoresystem.supply.domain.model.Supplier
import com.beerstoresystem.supply.infrastructure.persistence.entity.SupplierEntity

fun SupplierEntity.toDomain() = Supplier(
    id = id,
    name = name,
    countryId = countryId,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    createdAt = createdAt
)

fun Supplier.toEntity(): SupplierEntity = SupplierEntity().apply {
    if (this@toEntity.id != 0L) id = this@toEntity.id
    name = this@toEntity.name
    countryId = this@toEntity.countryId
    contactEmail = this@toEntity.contactEmail
    contactPhone = this@toEntity.contactPhone
    createdAt = this@toEntity.createdAt
}
