package com.example.drywall.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Modelo de relación uno a muchos entre un [ProjectEntity] y sus muros y vanos.
 *
 * Los vanos se cargan por `projectId` (columna denormalizada) para que la obra completa se
 * resuelva con dos relaciones planas, que es lo que Room soporta.
 */
data class ProjectWithCalculations(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val calculations: List<WallCalculationEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val openings: List<WallOpeningEntity>
) {
    /** Vanos agrupados por muro. */
    val openingsByWall: Map<Long, List<WallOpeningEntity>>
        get() = openings.groupBy { it.wallId }

    /** Vanos de un muro concreto. */
    fun openingsFor(wallId: Long): List<WallOpeningEntity> = openingsByWall[wallId].orEmpty()

    /** Compone un muro con sus vanos para recargarlo en la calculadora. */
    fun toWallWithOpenings(wall: WallCalculationEntity): WallWithOpenings =
        WallWithOpenings(wall = wall, openings = openingsFor(wall.id))

    /** Área neta total de la obra (todos los muros, descontando vanos). */
    val totalNetAreaM2: Double
        get() = calculations.sumOf { it.effectiveNetAreaM2 }

    /** Placas totales de la obra. */
    val totalBoards: Int
        get() = calculations.sumOf { it.totalBoardsCount }

    /** Perfiles de 3 m totales de la obra. */
    val totalProfiles3m: Int
        get() = calculations.sumOf { it.totalProfiles3mCount }

    /** Envases de pintura totales de la obra (sellador más acabado). */
    val totalPaintUnits: Int
        get() = calculations.sumOf { it.totalPaintUnits }

    /** Cantidad de vanos de la obra. */
    val totalOpenings: Int
        get() = openings.size
}
