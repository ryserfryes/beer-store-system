package com.beerstoresystem.warehouse.infrastructure.persistence.jpa

import com.beerstoresystem.warehouse.infrastructure.persistence.entity.InventoryBatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface InventoryBatchJpaRepository : JpaRepository<InventoryBatchEntity, Long> {

    fun findByWarehouseId(warehouseId: Long): List<InventoryBatchEntity>

    @Query("""
        SELECT b FROM InventoryBatchEntity b
        WHERE b.variantId = :variantId
          AND b.quantityOnHand > 0
          AND (b.expiresOn IS NULL OR b.expiresOn > :today)
        ORDER BY b.receivedAt ASC
    """)
    fun findAvailableByVariantIdFifo(
        @Param("variantId") variantId: Long,
        @Param("today") today: LocalDate
    ): List<InventoryBatchEntity>

    @Query("""
        SELECT COALESCE(SUM(b.quantityOnHand), 0)
        FROM InventoryBatchEntity b
        WHERE b.variantId = :variantId
          AND b.quantityOnHand > 0
          AND (b.expiresOn IS NULL OR b.expiresOn > :today)
    """)
    fun sumAvailableQuantityByVariantId(
        @Param("variantId") variantId: Long,
        @Param("today") today: LocalDate
    ): Int
}
