package com.example.drywall.data.mapper

import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.WallOpeningEntity
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.domain.model.PaintEstimate
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening

/**
 * Traducciones entre el modelo de dominio y las entidades Room.
 *
 * La persistencia guarda los parámetros de entrada completos (incluidos los vanos y la
 * configuración de pintura), de modo que al recargar un muro el cálculo se reproduce
 * exactamente igual.
 */

/** Vano persistido a modelo de dominio. */
fun WallOpeningEntity.toDomain(): WallOpening = WallOpening(
    type = runCatching { OpeningType.valueOf(type) }.getOrDefault(OpeningType.PUERTA),
    label = label,
    widthMeters = widthMeters,
    heightMeters = heightMeters
)

/** Reconstruye la entrada de cálculo de un muro persistido. */
fun WallCalculationEntity.toInput(openings: List<WallOpeningEntity> = emptyList()): WallCalculationInput =
    WallCalculationInput(
        lengthMeters = lengthMeters,
        heightMeters = heightMeters,
        faces = runCatching { WallFaces.valueOf(faces) }.getOrDefault(WallFaces.BOTH_FACES),
        hasInsulation = hasInsulation,
        studSpacingMeters = studSpacingMeters,
        boardType = runCatching { DrywallBoardType.valueOf(boardType) }
            .getOrDefault(DrywallBoardType.ESTANDAR_ST),
        wasteFactor = wasteFactor,
        openings = openings.map { it.toDomain() }
    )

/** Reconstruye la configuración de pintura persistida del muro. */
fun WallCalculationEntity.toPaintSpec(): PaintSpec = PaintSpec(
    finishCoats = paintFinishCoats,
    wasteFactor = paintWasteFactor,
    product = PaintProduct(
        name = paintProductName,
        unit = runCatching { PaintUnit.valueOf(paintUnit) }.getOrDefault(PaintUnit.GALON),
        coverageM2PerUnit = paintCoverageM2PerUnit
    )
)

/** Reconstruye la estimación de pintura persistida, o `null` si el muro no lleva pintura. */
fun WallCalculationEntity.toPaintEstimate(): PaintEstimate? {
    if (!paintEnabled || paintSealerUnits + paintFinishUnits <= 0) return null
    return PaintEstimate(
        areaM2 = paintAreaM2,
        sealerCoats = PaintSpec.SEALER_COATS,
        finishCoats = paintFinishCoats,
        coverageM2PerUnit = paintCoverageM2PerUnit,
        unit = runCatching { PaintUnit.valueOf(paintUnit) }.getOrDefault(PaintUnit.GALON),
        wasteFactor = paintWasteFactor,
        sealerUnits = paintSealerUnits,
        finishUnits = paintFinishUnits,
        productName = paintProductName
    )
}

/** Convierte el resultado del cálculo en la fila de muro a persistir. */
fun WallCalculationResult.toEntity(
    projectId: Long,
    wallName: String,
    createdAt: Long = System.currentTimeMillis()
): WallCalculationEntity {
    val paintEstimate = paint
    return WallCalculationEntity(
        projectId = projectId,
        wallName = wallName,
        lengthMeters = input.lengthMeters,
        heightMeters = input.heightMeters,
        faces = input.faces.name,
        hasInsulation = input.hasInsulation,
        studSpacingMeters = input.studSpacingMeters,
        boardType = input.boardType.name,
        wasteFactor = input.wasteFactor,
        totalDrywallAreaM2 = input.totalDrywallArea,
        netAreaM2 = input.netTotalDrywallArea,
        totalBoardsCount = totalBoardsRequired,
        totalProfiles3mCount = totalProfiles3mRequired,
        summaryNotes = buildSummaryNotes(),
        createdAt = createdAt,
        paintEnabled = paintEstimate != null,
        paintFinishCoats = paintEstimate?.finishCoats ?: PaintSpec.DEFAULT_FINISH_COATS,
        paintWasteFactor = paintEstimate?.wasteFactor ?: PaintSpec.DEFAULT_WASTE_FACTOR,
        paintProductName = paintEstimate?.productName ?: "",
        paintUnit = (paintEstimate?.unit ?: PaintUnit.GALON).name,
        paintCoverageM2PerUnit = paintEstimate?.coverageM2PerUnit ?: 0.0,
        paintAreaM2 = paintEstimate?.areaM2 ?: 0.0,
        paintSealerUnits = paintEstimate?.sealerUnits ?: 0,
        paintFinishUnits = paintEstimate?.finishUnits ?: 0
    )
}

/** Vanos del resultado listos para insertar con la llave del muro recién creado. */
fun WallCalculationResult.toOpeningEntities(wallId: Long, projectId: Long): List<WallOpeningEntity> =
    input.openings.map { opening ->
        WallOpeningEntity(
            wallId = wallId,
            projectId = projectId,
            type = opening.type.name,
            label = opening.label,
            widthMeters = opening.widthMeters,
            heightMeters = opening.heightMeters
        )
    }

private fun WallCalculationResult.buildSummaryNotes(): String = buildString {
    append("Placas: $totalBoardsRequired unid, Perfiles 3m: $totalProfiles3mRequired unid")
    if (openingsCount > 0) {
        append(", Vanos: $openingsCount")
    }
    paint?.let {
        append(", Pintura: ${it.totalUnits} ${it.unitLabelForTotal}")
    }
}
