package com.beerstoresystem.order.domain.port

data class VariantInfo(
    val id: Long,
    val sku: String,
    val unitPrice: Double,
    val beerId: Long?
)

interface CatalogPort {
    fun getVariants(ids: List<Long>): List<VariantInfo>
}
