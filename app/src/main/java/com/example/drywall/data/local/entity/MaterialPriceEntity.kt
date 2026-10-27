package com.example.drywall.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Precio unitario cotizado de un insumo.
 *
 * `itemKey` es el `id` del material, o `paint_sealer:<UNIDAD>` / `paint_finish:<UNIDAD>`
 * para la pintura. `unitPrice <= 0` significa **sin cotizar**.
 */
@Entity(tableName = "material_prices")
data class MaterialPriceEntity(
    @PrimaryKey
    val itemKey: String,
    val unitPrice: Double = 0.0,
    val unitLabel: String = "",
    val updatedAt: Long = 0L
)
