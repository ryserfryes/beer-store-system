package com.beerstoresystem.supply.domain.model

enum class PurchaseOrderStatus {
    WAITING_APPROVAL,
    DRAFTING,
    PARTIALLY_DELIVERED,
    DELIVERED,
    RECEIVED,
    CANCELED
}
