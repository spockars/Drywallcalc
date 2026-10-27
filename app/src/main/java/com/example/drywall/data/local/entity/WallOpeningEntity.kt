package com.example.drywall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vano (puerta o ventana) persistido de un muro.
 *
 * `projectId` se denormaliza para que la obra pueda cargar todos sus vanos con una sola
 * relación de Room (Room no admite relaciones anidadas hacia POJOs). Ambas llaves foráneas
 * borran en cascada, así que nunca quedan vanos huérfanos.
 */
@Entity(
    tableName = "wall_openings",
    foreignKeys = [
        ForeignKey(
            entity = WallCalculationEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wallId"]),
        Index(value = ["projectId"])
    ]
)
data class WallOpeningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallId: Long,
    val projectId: Long,
    val type: String, // OpeningType.name
    val label: String = "",
    val widthMeters: Double,
    val heightMeters: Double
)

/**
 * Área neta del muro. Las filas heredadas de la versión 1 de la base tienen `netAreaM2 = 0`
 * porque se escribieron antes de existir los vanos; en esos casos el área total es equivalente.
 */
val WallCalculationEntity.effectiveNetAreaM2: Double
    get() = if (netAreaM2 > 0.0) netAreaM2 else totalDrywallAreaM2

/** Envases de pintura del muro: sellador más manos de acabado. */
val WallCalculationEntity.totalPaintUnits: Int
    get() = paintSealerUnits + paintFinishUnits
