package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.engine.CostCalculator
import com.example.drywall.domain.engine.PaintCalculator
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.LaborMode
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintRole
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.PriceEntry
import com.example.drywall.domain.model.PriceKeys
import com.example.drywall.domain.model.ProjectLabor
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening
import com.example.drywall.domain.model.OpeningType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de costos y presupuesto.
 *
 * Regla bajo prueba: se multiplica la cantidad **comercial** (la redondeada) por el precio
 * unitario, y un insumo sin precio marca el presupuesto como incompleto en vez de sumar 0.
 */
class CostCalculatorTest {

    private val engine = CalculatorEngine()

    private val precios = mapOf(
        "canal_guia_3m" to PriceEntry("canal_guia_3m", 20_000.0),
        "poste_montante_3m" to PriceEntry("poste_montante_3m", 18_000.0),
        "placa_st_12_7" to PriceEntry("placa_st_12_7", 62_000.0),
        "tornillo_t1" to PriceEntry("tornillo_t1", 200.0),
        "tornillo_t2" to PriceEntry("tornillo_t2", 250.0),
        "anclaje_chazo" to PriceEntry("anclaje_chazo", 800.0),
        "cinta_juntas" to PriceEntry("cinta_juntas", 15_000.0),
        "masilla_juntas" to PriceEntry("masilla_juntas", 45_000.0)
    )

    /** Muro 4.0 × 2.6 a dos caras con una puerta de 0.90 × 2.10. */
    private fun muroConPuerta(): WallCalculationResult = engine.calculate(
        WallCalculationInput(
            lengthMeters = 4.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = false,
            studSpacingMeters = 0.407,
            boardType = DrywallBoardType.ESTANDAR_ST,
            wasteFactor = 0.08,
            openings = listOf(WallOpening(OpeningType.PUERTA, "Puerta", 0.9, 2.1))
        )
    )

    @Test
    fun testCantidadComercialPorPrecioUnitario() {
        val result = muroConPuerta()
        val cost = CostCalculator.buildWallCost(result, precios)

        // Las placas son 7 comerciales (no 5.72 teóricas)
        val boards = cost.lines.first { it.itemKey == "placa_st_12_7" }
        assertEquals(7, boards.commercialQuantity)
        assertEquals(7 * 62_000.0, boards.subtotal, 0.01)

        // 4 canal + 15 postes + 7 placas + 57 T1 + 227 T2 + 20 anclajes + 1 cinta + 1 masilla
        assertEquals(928_150.0, cost.materialsSubtotal, 0.01)
        assertTrue(cost.complete)
        assertEquals(0, cost.missingPrices)
    }

    @Test
    fun testLosPreciosSeMultiplicanPorLaCantidadComercialYNoPorLaTeorica() {
        val result = muroConPuerta()
        val cost = CostCalculator.buildWallCost(result, precios)

        result.requirements.forEach { requirement ->
            val line = cost.lines.first { it.itemKey == requirement.material.id }
            assertEquals(requirement.commercialQuantity, line.commercialQuantity)
            // La teórica nunca se usa para cobrar
            assertNotEquals(requirement.rawQuantity, line.commercialQuantity.toDouble(), 0.0001)
        }
    }

    @Test
    fun testPresupuestoIncompletoCuandoFaltaUnPrecio() {
        val result = muroConPuerta()
        val sinMasilla = precios - "masilla_juntas"
        val cost = CostCalculator.buildWallCost(result, sinMasilla)

        assertFalse(cost.complete)
        assertEquals(1, cost.missingPrices)
        // El parcial descuenta la línea sin precio pero no la suma como cero silencioso
        assertEquals(928_150.0 - 45_000.0, cost.materialsSubtotal, 0.01)

        val line = cost.lines.first { it.itemKey == "masilla_juntas" }
        assertFalse(line.isPriced)
        assertEquals(0.0, line.subtotal, 0.0001)
    }

    @Test
    fun testPresupuestoCompletoSumaMaterialesYTotal() {
        val cost = CostCalculator.buildWallCost(muroConPuerta(), precios)
        val area = muroConPuerta().netTotalDrywallArea

        val budget = CostCalculator.buildBudget(
            wallCosts = listOf(cost),
            labor = ProjectLabor(enabled = true, mode = LaborMode.PER_M2, amount = 25_000.0),
            netAreaM2 = area
        )

        assertEquals(17.02, area, 0.0001)
        assertEquals(928_150.0, budget.materialsSubtotal, 0.01)
        assertEquals(17.02 * 25_000.0, budget.labor, 0.01)
        assertEquals(928_150.0 + 425_500.0, budget.total, 0.01)
        assertTrue(budget.complete)
    }

    @Test
    fun testManoDeObraEnModoGlobal() {
        val cost = CostCalculator.buildWallCost(muroConPuerta(), precios)
        val budget = CostCalculator.buildBudget(
            wallCosts = listOf(cost),
            labor = ProjectLabor(enabled = true, mode = LaborMode.GLOBAL, amount = 1_000_000.0),
            netAreaM2 = 17.02
        )

        assertEquals(1_000_000.0, budget.labor, 0.01)
        assertEquals(928_150.0 + 1_000_000.0, budget.total, 0.01)
    }

    @Test
    fun testManoDeObraApagadaNoSuma() {
        val cost = CostCalculator.buildWallCost(muroConPuerta(), precios)
        val budget = CostCalculator.buildBudget(
            wallCosts = listOf(cost),
            labor = ProjectLabor(enabled = false, mode = LaborMode.PER_M2, amount = 25_000.0),
            netAreaM2 = 17.02
        )

        assertEquals(0.0, budget.labor, 0.0001)
        assertEquals(928_150.0, budget.total, 0.01)
    }

    @Test
    fun testPinturaSeCotizaPorRolYPorUnidad() {
        val base = muroConPuerta()
        val spec = PaintSpec(
            finishCoats = 2,
            wasteFactor = 0.10,
            product = PaintProduct(name = "Acrílica", unit = PaintUnit.GALON, coverageM2PerUnit = 10.0)
        )
        val result = base.copy(paint = PaintCalculator.estimate(base.netTotalDrywallArea, spec))

        val sealerKey = PriceKeys.paint(PaintRole.SELLADOR, PaintUnit.GALON)
        val finishKey = PriceKeys.paint(PaintRole.ACABADO, PaintUnit.GALON)
        assertNotEquals(sealerKey, finishKey)
        assertNotEquals(sealerKey, PriceKeys.paint(PaintRole.SELLADOR, PaintUnit.LITRO))

        val cost = CostCalculator.buildWallCost(
            result,
            precios + mapOf(
                sealerKey to PriceEntry(sealerKey, 95_000.0),
                finishKey to PriceEntry(finishKey, 120_000.0)
            )
        )

        val sealer = cost.lines.first { it.itemKey == sealerKey }
        val finish = cost.lines.first { it.itemKey == finishKey }

        assertEquals(result.paint!!.sealerUnits, sealer.commercialQuantity)
        assertEquals(result.paint!!.finishUnits, finish.commercialQuantity)
        assertEquals("galones", sealer.unitLabel)
        assertTrue(cost.complete)
        assertEquals(
            928_150.0 + result.paint!!.sealerUnits * 95_000.0 + result.paint!!.finishUnits * 120_000.0,
            cost.materialsSubtotal,
            0.01
        )
    }

    @Test
    fun testSinPrecioDePinturaElPresupuestoQuedaIncompleto() {
        val base = muroConPuerta()
        val spec = PaintSpec(
            product = PaintProduct(name = "Acrílica", unit = PaintUnit.LITRO, coverageM2PerUnit = 8.0)
        )
        val result = base.copy(paint = PaintCalculator.estimate(base.netTotalDrywallArea, spec))

        // Los precios de materiales están completos, pero no hay precio de pintura en litros
        val cost = CostCalculator.buildWallCost(result, precios)

        assertFalse(cost.complete)
        assertEquals(2, cost.missingPrices)
        assertEquals(928_150.0, cost.materialsSubtotal, 0.01)
    }
}
