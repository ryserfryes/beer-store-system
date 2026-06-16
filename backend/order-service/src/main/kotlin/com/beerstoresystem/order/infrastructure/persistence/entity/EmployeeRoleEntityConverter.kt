package com.beerstoresystem.order.infrastructure.persistence.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class EmployeeRoleEntityConverter : AttributeConverter<EmployeeRoleEntity, String> {

    override fun convertToDatabaseColumn(attribute: EmployeeRoleEntity?): String? =
        attribute?.name?.lowercase()

    override fun convertToEntityAttribute(dbData: String?): EmployeeRoleEntity? =
        dbData?.uppercase()?.let { EmployeeRoleEntity.valueOf(it) }
}
