package com.example.drywall.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Modelo de relación uno a muchos entre un [ProjectEntity] y sus [WallCalculationEntity] asociados.
 */
data class ProjectWithCalculations(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val calculations: List<WallCalculationEntity>
) {
    val totalAreaM2: Double
        get() = calculations.sumOf { it.totalDrywallAreaM2 }

    val totalBoards: Int
        get() = calculations.sumOf { it.totalBoardsCount }

    val totalProfiles3m: Int
        get() = calculations.sumOf { it.totalProfiles3mCount }
}
