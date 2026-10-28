package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.DrywallCeilingInput
import com.example.drywall.domain.model.PvcCeilingInput
import com.example.drywall.domain.model.FurnitureCalculationInput
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementCalculatorEnginesTest {

    private val engine = CalculatorEngine()

    @Test
    fun testDrywallCeilingCalculation() {
        val input = DrywallCeilingInput(
            lengthMeters = 5.0,
            widthMeters = 4.0,
            suspensionHeightMeters = 0.40,
            hasInsulation = true
        )
        val requirements = engine.calculateDrywallCeiling(input)
        assertTrue(requirements.isNotEmpty())
        val boards = requirements.firstOrNull { it.material.id.contains("placa") }
        assertTrue(boards != null && boards.commercialQuantity > 0)
    }

    @Test
    fun testPvcCeilingCalculation() {
        val input = PvcCeilingInput(
            lengthMeters = 4.0,
            widthMeters = 3.0
        )
        val requirements = engine.calculatePvcCeiling(input)
        assertTrue(requirements.isNotEmpty())
        val pvcTablillas = requirements.firstOrNull { it.material.id.contains("tablilla") }
        assertTrue(pvcTablillas != null && pvcTablillas.commercialQuantity > 0)
    }

    @Test
    fun testFurnitureCalculation() {
        val input = FurnitureCalculationInput(
            widthMeters = 2.0,
            heightMeters = 1.8,
            depthMeters = 0.40,
            horizontalShelvesCount = 2,
            verticalDividersCount = 1
        )
        val requirements = engine.calculateFurniture(input)
        assertTrue(requirements.isNotEmpty())
        val cornerBeads = requirements.firstOrNull { it.material.id.contains("esquinero") }
        assertTrue(cornerBeads != null && cornerBeads.commercialQuantity > 0)
    }
}
