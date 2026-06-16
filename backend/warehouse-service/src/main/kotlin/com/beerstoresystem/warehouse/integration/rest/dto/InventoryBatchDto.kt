package com.beerstoresystem.warehouse.integration.rest.dto

import java.time.LocalDate

data class InventoryBatchResponse(
    val id: Long,
    val variantId: Long,
    val lotCode: String?,
    val quantityOnHand: Int,
    val expiresOn: LocalDate?
)
