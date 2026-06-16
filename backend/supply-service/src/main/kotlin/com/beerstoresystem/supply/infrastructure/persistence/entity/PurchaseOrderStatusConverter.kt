package com.beerstoresystem.supply.infrastructure.persistence.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.beerstoresystem.supply.domain.model.PurchaseOrderStatus

@Converter(autoApply = false)
class PurchaseOrderStatusConverter : AttributeConverter<PurchaseOrderStatus, String> {

    override fun convertToDatabaseColumn(attribute: PurchaseOrderStatus?): String? =
        attribute?.name?.lowercase()

    override fun convertToEntityAttribute(dbData: String?): PurchaseOrderStatus? =
        dbData?.let { PurchaseOrderStatus.valueOf(it.uppercase()) }
}
