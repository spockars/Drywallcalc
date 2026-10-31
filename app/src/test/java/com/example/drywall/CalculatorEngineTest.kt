package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.UnitOfMeasure
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para la lógica de negocio de cálculo de Drywall y su integración con el ViewModel.
 */
class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    @Test
    fun testStandardTwoFaceWallCalculation_with8PercentWaste() {
        // Muro de 5.0 metros de largo por 2.6 metros de alto, a 2 caras (divisorio), con aislamiento
        val input = WallCalculationInput(
            lengthMeters = 5.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = true,
            studSpacingMeters = 0.407,
            boardType = DrywallBoardType.ESTANDAR_ST,
            wasteFactor = 0.08 // 8% de desperdicio exigido
        )

        val result = engine.calculate(input)

        // Verificaciones geométricas básicas
        assertEquals(13.0, result.input.wallAreaOneFace, 0.001) // 5.0 * 2.6 = 13.0 m²
        assertEquals(26.0, result.input.totalDrywallArea, 0.001) // 13.0 * 2 = 26.0 m²

        // 1. Placas de yeso:
        // Área total: 26.0 m² / (1.22 * 2.44) = ~8.734 placas base.
        // Con 8% desperdicio: 8.734 * 1.08 = 9.43 placas -> Redondeo comercial = 10 placas estándar.
        val boardReq = result.requirements.first { it.material.category == MaterialCategory.PLACAS }
        assertEquals(10, boardReq.commercialQuantity)
        assertEquals(UnitOfMeasure.PLACAS_ESTANDAR, boardReq.unit)
        assertEquals(0.08, boardReq.wastePercentage, 0.0001)

        // 2. Canales / Soleras (piso y techo):
        // 5.0 m * 2 = 10.0 m lineales. Con 8% desperdicio = 10.8 m.
        // En perfiles comerciales de 2.44 m (estándar Colombia): ceil(10.8 / 2.44) = 5 piezas.
        val trackReq = result.requirements.first { it.material.id.startsWith("canal_guia") }
        assertEquals(5, trackReq.commercialQuantity)
        assertEquals(UnitOfMeasure.PIEZAS_2_44M, trackReq.unit)

        // 3. Postes / Montantes verticales:
        // Largo 5.0 / 0.407 = ~12.28 -> 13 + 1 (arranque) = 14 postes * 2.6m = 36.4m * 1.08 = 39.31m.
        // En perfiles comerciales de 2.44 m: ceil(39.31 / 2.44) = 17 piezas.
        val studReq = result.requirements.first { it.material.id.startsWith("poste_montante") }
        assertEquals(17, studReq.commercialQuantity)

        // 4. Aislamiento termoacústico:
        // Área 13.0 m² * 1.08 = 14.04 m². Rollos de 12 m²: ceil(14.04 / 12) = 2 rollos.
        val insulationReq = result.requirements.firstOrNull { it.material.category == MaterialCategory.AISLAMIENTO }
        assertNotNull(insulationReq)
        assertEquals(2, insulationReq?.commercialQuantity)

        // 5. Fijaciones:
        // Tornillos T1 (4 por montante): 14 * 4 = 56 * 1.08 = 60.48 -> ceil = 61 unidades.
        val t1Req = result.requirements.first { it.material.id == "tornillo_t1" }
        assertEquals(61, t1Req.commercialQuantity)

        // Tornillos T2 (30 por placa): 10 placas * 30 = 300 * 1.08 = 324 unidades.
        val t2Req = result.requirements.first { it.material.id == "tornillo_t2" }
        assertEquals(324, t2Req.commercialQuantity)
    }

    @Test
    fun testSingleFaceWallWithoutInsulation_omitsInsulation() {
        // Muro de revestimiento a 1 cara sin aislamiento
        val input = WallCalculationInput(
            lengthMeters = 3.0,
            heightMeters = 2.4,
            faces = WallFaces.ONE_FACE,
            hasInsulation = false,
            wasteFactor = 0.08
        )

        val result = engine.calculate(input)

        assertEquals(7.2, result.input.wallAreaOneFace, 0.001)
        assertEquals(7.2, result.input.totalDrywallArea, 0.001)

        // No debe incluir aislamiento en la lista
        val insulationReq = result.requirements.firstOrNull { it.material.category == MaterialCategory.AISLAMIENTO }
        assertNull(insulationReq)

        // Placas: 7.2 / 2.9768 = ~2.41 placas * 1.08 = 2.61 -> ceil = 3 placas
        val boardReq = result.requirements.first { it.material.category == MaterialCategory.PLACAS }
        assertEquals(3, boardReq.commercialQuantity)
    }

    @Test
    fun testViewModel_reactiveStateAndCalculation() {
        val viewModel = DrywallCalculatorViewModel(engine)

        // Verifica estado inicial
        val initialState = viewModel.uiState.value
        assertNotNull(initialState.calculationResult)
        assertEquals("4.0", initialState.lengthInput)
        assertEquals("2.6", initialState.heightInput)

        // Modifica longitud a 6 metros
        viewModel.onLengthChanged("6.0")
        val updatedState = viewModel.uiState.value
        assertEquals("6.0", updatedState.lengthInput)
        assertNotNull(updatedState.calculationResult)
        assertEquals(6.0, updatedState.calculationResult?.input?.lengthMeters ?: 0.0, 0.001)

        // Entrada inválida (texto no numérico)
        viewModel.onLengthChanged("abc")
        val errorState = viewModel.uiState.value
        assertNotNull(errorState.errorMessage)
        assertNull(errorState.calculationResult)

        // Recuperación con preset
        viewModel.applyPreset(length = 3.5, height = 2.5, isTwoFaces = false, withInsulation = false)
        val presetState = viewModel.uiState.value
        assertNull(presetState.errorMessage)
        assertNotNull(presetState.calculationResult)
        assertEquals(3.5, presetState.calculationResult?.input?.lengthMeters ?: 0.0, 0.001)
        assertEquals(WallFaces.ONE_FACE, presetState.faces)

        // Verificación de compatibilidad con coma decimal (teclados hispanos ej: "4,5")
        viewModel.onLengthChanged("4,5")
        viewModel.onHeightChanged("2,8")
        val commaState = viewModel.uiState.value
        assertNull(commaState.errorMessage)
        assertNotNull(commaState.calculationResult)
        assertEquals(4.5, commaState.calculationResult?.input?.lengthMeters ?: 0.0, 0.001)
        assertEquals(2.8, commaState.calculationResult?.input?.heightMeters ?: 0.0, 0.001)
    }
}

