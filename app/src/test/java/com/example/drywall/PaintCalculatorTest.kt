package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.engine.PaintCalculator
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallFaces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del calculador de pintura, que vive separado del motor de drywall.
 *
 * Consumo = área neta × manos × (1 + merma) ÷ rendimiento del producto, redondeado a
 * envases enteros por etapa (sellador y acabado).
 */
class PaintCalculatorTest {

    private fun spec(
        coverage: Double = 10.0,
        finishCoats: Int = 2,
        waste: Double = 0.10,
        unit: PaintUnit = PaintUnit.GALON
    ) = PaintSpec(
        finishCoats = finishCoats,
        wasteFactor = waste,
        product = PaintProduct(name = "Acrílica tipo 1", unit = unit, coverageM2PerUnit = coverage)
    )

    @Test
    fun testConsumoPorManosYRendimiento() {
        // 26 m², 1 mano de sellador y 2 de acabado, 10 % de merma, 10 m² por galón
        val estimate = PaintCalculator.estimate(areaM2 = 26.0, spec = spec())

        assertEquals(26.0, estimate.areaM2, 0.0001)
        assertEquals(1, estimate.sealerCoats)
        assertEquals(2, estimate.finishCoats)
        // Sellador: 26 * 1 * 1.10 / 10 = 2.86 => 3 galones
        assertEquals(3, estimate.sealerUnits)
        // Acabado: 26 * 2 * 1.10 / 10 = 5.72 => 6 galones
        assertEquals(6, estimate.finishUnits)
        assertEquals(9, estimate.totalUnits)
        assertEquals("galones", estimate.unitLabelForTotal)
        assertTrue(estimate.isValid)
    }

    @Test
    fun testRendimientoEsEditablePorProducto() {
        // El mismo muro con un rendimiento mayor requiere menos envases
        val economical = PaintCalculator.estimate(26.0, spec(coverage = 20.0))
        val expensive = PaintCalculator.estimate(26.0, spec(coverage = 5.0))

        assertEquals(2, economical.sealerUnits) // 26*1.1/20 = 1.43 => 2
        assertEquals(3, economical.finishUnits) // 26*2*1.1/20 = 2.86 => 3
        assertEquals(6, expensive.sealerUnits)  // 26*1.1/5 = 5.72 => 6
        assertEquals(12, expensive.finishUnits) // 26*2*1.1/5 = 11.44 => 12
    }

    @Test
    fun testUnaSolaManoDeAcabadoYUnidadesEnLitros() {
        val estimate = PaintCalculator.estimate(
            areaM2 = 10.0,
            spec = spec(coverage = 4.0, finishCoats = 1, waste = 0.0, unit = PaintUnit.LITRO)
        )

        assertEquals(3, estimate.sealerUnits)  // 10 * 1 / 4 = 2.5 => 3 envases
        assertEquals(3, estimate.finishUnits)  // 10 * 1 / 4 = 2.5 => 3 envases
        assertEquals("litros", estimate.unitLabelForTotal)
    }

    @Test
    fun testConfiguracionInvalidaSeRechazaConMensaje() {
        assertEquals(
            listOf("Ingresa el rendimiento del producto en m² por galón."),
            PaintCalculator.validate(areaM2 = 26.0, spec = spec(coverage = 0.0))
        )
        assertTrue(PaintCalculator.validate(areaM2 = 0.0, spec = spec()).isNotEmpty())
        assertTrue(PaintCalculator.validate(26.0, spec(finishCoats = 0)).isNotEmpty())

        assertThrows(IllegalArgumentException::class.java) {
            PaintCalculator.estimate(areaM2 = 26.0, spec = spec(coverage = 0.0))
        }
    }

    @Test
    fun testElMotorDeDrywallNoCalculaPintura() {
        // Responsabilidad separada: el resultado del motor no trae pintura por sí solo
        val result = CalculatorEngine().calculate(
            WallCalculationInput(
                lengthMeters = 5.0,
                heightMeters = 2.6,
                faces = WallFaces.BOTH_FACES,
                hasInsulation = true,
                boardType = DrywallBoardType.ESTANDAR_ST,
                wasteFactor = 0.08
            )
        )

        assertNull(result.paint)
        assertFalse(result.requirements.any { it.material.category.name == "PINTURA" })
    }
}
