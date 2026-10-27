package com.example.drywall.domain.engine

import com.example.drywall.domain.model.Budget
import com.example.drywall.domain.model.CostLine
import com.example.drywall.domain.model.PaintRole
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.PriceEntry
import com.example.drywall.domain.model.PriceKeys
import com.example.drywall.domain.model.ProjectLabor
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallCost

/**
 * Calculadora de costos y presupuesto, independiente del motor de materiales.
 *
 * Regla central: el precio se multiplica por la cantidad **comercial** (la redondeada),
 * nunca por la cantidad teórica. Un insumo sin precio no suma 0 en silencio: se cuenta
 * como faltante y el presupuesto se marca como incompleto.
 */
object CostCalculator {

    /** Construye el desglose de costo de un muro a partir de su cómputo y el catálogo. */
    fun buildWallCost(
        result: WallCalculationResult,
        prices: Map<String, PriceEntry>
    ): WallCost {
        val lines = mutableListOf<CostLine>()

        result.requirements.forEach { requirement ->
            val itemKey = PriceKeys.material(requirement.material.id)
            lines += CostLine(
                itemKey = itemKey,
                name = requirement.material.name,
                unitLabel = requirement.unitLabel,
                commercialQuantity = requirement.commercialQuantity,
                unitPrice = prices[itemKey]?.unitPrice ?: 0.0
            )
        }

        result.paint?.let { paint ->
            val sealerKey = PriceKeys.paint(PaintRole.SELLADOR, paint.unit)
            val finishKey = PriceKeys.paint(PaintRole.ACABADO, paint.unit)
            lines += CostLine(
                itemKey = sealerKey,
                name = "Pintura — sellador (${paint.sealerCoats} mano)",
                unitLabel = paintUnitLabel(paint.unit, paint.sealerUnits),
                commercialQuantity = paint.sealerUnits,
                unitPrice = prices[sealerKey]?.unitPrice ?: 0.0
            )
            lines += CostLine(
                itemKey = finishKey,
                name = "Pintura — acabado (${paint.finishCoats} manos)",
                unitLabel = paintUnitLabel(paint.unit, paint.finishUnits),
                commercialQuantity = paint.finishUnits,
                unitPrice = prices[finishKey]?.unitPrice ?: 0.0
            )
        }

        return WallCost(lines = lines)
    }

    /**
     * Presupuesto de una obra: suma de materiales de todos sus muros más la mano de obra.
     *
     * @param netAreaM2 área neta total de la obra, base de la mano de obra por m².
     */
    fun buildBudget(
        wallCosts: List<WallCost>,
        labor: ProjectLabor,
        netAreaM2: Double
    ): Budget = Budget(
        materialsSubtotal = wallCosts.sumOf { it.materialsSubtotal },
        labor = labor.costFor(netAreaM2),
        missingPrices = wallCosts.sumOf { it.missingPrices }
    )

    /** Etiqueta de la unidad del envase de pintura concordante con la cantidad. */
    fun paintUnitLabel(unit: PaintUnit, units: Int): String =
        if (units == 1) unit.label.lowercase() else unit.pluralLabel.lowercase()
}
