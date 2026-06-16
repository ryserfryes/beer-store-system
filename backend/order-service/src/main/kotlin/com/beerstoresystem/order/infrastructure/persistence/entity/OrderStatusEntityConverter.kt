package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class OrderStatusEntityConverter : AttributeConverter<OrderStatusEntity, String> {

    override fun convertToDatabaseColumn(attribute: OrderStatusEntity?): String? =
        attribute?.name?.lowercase()

    override fun convertToEntityAttribute(dbData: String?): OrderStatusEntity? =
        dbData?.uppercase()?.let { OrderStatusEntity.valueOf(it) }
}
