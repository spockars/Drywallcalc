package com.example.drywall.data.local.entity

/**
 * Muro con sus vanos ya resueltos, listo para reproducir el cálculo al recargar un proyecto.
 */
data class WallWithOpenings(
    val wall: WallCalculationEntity,
    val openings: List<WallOpeningEntity>
) {
    /** Área neta del muro, con retrocompatibilidad para filas heredadas de la versión 1. */
    val effectiveNetAreaM2: Double
        get() = wall.effectiveNetAreaM2

    /** Envases de pintura del muro, sumando sellador y acabado. */
    val totalPaintUnits: Int
        get() = wall.totalPaintUnits
}
