package com.example.drywall.data.mapper

import com.example.drywall.data.local.entity.MaterialPriceEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.effectiveNetAreaM2
import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.engine.CostCalculator
import com.example.drywall.domain.model.Budget
import com.example.drywall.domain.model.ConstructionElementType
import com.example.drywall.domain.model.LaborMode
import com.example.drywall.domain.model.PriceEntry
import com.example.drywall.domain.model.ProjectLabor
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallCost

/** Precio persistido a modelo de dominio. */
fun MaterialPriceEntity.toDomain(): PriceEntry = PriceEntry(
    itemKey = itemKey,
    unitPrice = unitPrice,
    unitLabel = unitLabel,
    updatedAt = updatedAt
)

/** Precio de dominio a fila persistible. */
fun PriceEntry.toEntity(): MaterialPriceEntity = MaterialPriceEntity(
    itemKey = itemKey,
    unitPrice = unitPrice,
    unitLabel = unitLabel,
    updatedAt = updatedAt
)

/**
 * Presupuesto de una obra ya resuelto: costo por muro y total agregado.
 */
data class ProjectBudgetView(
    val projectId: Long,
    val wallCosts: Map<Long, WallCost>,
    val wallResults: Map<Long, WallCalculationResult>,
    val labor: ProjectLabor,
    val budget: Budget
)

/** Configuración de mano de obra persistida de una obra. */
fun ProjectWithCalculations.toLabor(): ProjectLabor = ProjectLabor(
    enabled = project.laborEnabled,
    mode = runCatching { LaborMode.valueOf(project.laborMode) }
        .getOrDefault(LaborMode.PER_M2),
    amount = project.laborAmount
)

/**
 * Reconstruye el cómputo de cada elemento (muros, cielos rasos, muebles) y calcula su costo
 * con el catálogo de precios vigente.
 */
fun ProjectWithCalculations.toBudgetView(
    engine: CalculatorEngine,
    prices: Map<String, PriceEntry>
): ProjectBudgetView {
    val wallCosts = mutableMapOf<Long, WallCost>()
    val wallResults = mutableMapOf<Long, WallCalculationResult>()

    calculations.forEach { wall ->
        val result = when (wall.elementType) {
            ConstructionElementType.CIELO_RASO_DRYWALL.name -> {
                val ceilingInput = wall.toDrywallCeilingInput()
                val reqs = engine.calculateDrywallCeiling(ceilingInput)
                val dummyWallInput = WallCalculationInput(
                    lengthMeters = ceilingInput.lengthMeters,
                    heightMeters = ceilingInput.widthMeters,
                    wasteFactor = ceilingInput.wasteFactor
                )
                WallCalculationResult(
                    input = dummyWallInput,
                    requirements = reqs,
                    paint = wall.toPaintEstimate()
                )
            }
            ConstructionElementType.CIELO_RASO_PVC.name -> {
                val pvcInput = wall.toPvcCeilingInput()
                val reqs = engine.calculatePvcCeiling(pvcInput)
                val dummyWallInput = WallCalculationInput(
                    lengthMeters = pvcInput.lengthMeters,
                    heightMeters = pvcInput.widthMeters,
                    wasteFactor = pvcInput.wasteFactor
                )
                WallCalculationResult(
                    input = dummyWallInput,
                    requirements = reqs
                )
            }
            ConstructionElementType.MUEBLE_DRYWALL.name -> {
                val furnitureInput = wall.toFurnitureInput()
                val reqs = engine.calculateFurniture(furnitureInput)
                val dummyWallInput = WallCalculationInput(
                    lengthMeters = furnitureInput.widthMeters,
                    heightMeters = furnitureInput.heightMeters,
                    wasteFactor = furnitureInput.wasteFactor
                )
                WallCalculationResult(
                    input = dummyWallInput,
                    requirements = reqs
                )
            }
            else -> {
                engine
                    .calculate(wall.toInput(openingsFor(wall.id)))
                    .copy(paint = wall.toPaintEstimate())
            }
        }
        wallResults[wall.id] = result
        wallCosts[wall.id] = CostCalculator.buildWallCost(result, prices)
    }

    val labor = toLabor()
    val netArea = calculations.sumOf { it.effectiveNetAreaM2 }

    return ProjectBudgetView(
        projectId = project.id,
        wallCosts = wallCosts,
        wallResults = wallResults,
        labor = labor,
        budget = CostCalculator.buildBudget(wallCosts.values.toList(), labor, netArea)
    )
}
