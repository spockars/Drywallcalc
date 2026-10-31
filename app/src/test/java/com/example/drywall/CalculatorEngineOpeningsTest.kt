package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.MaterialRequirement
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel
import com.example.drywall.util.DrywallReportExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas del cálculo de materiales con vanos (puertas y ventanas).
 *
 * Los números esperados están derivados de las reglas de negocio:
 * área neta para placas y aislamiento, cinta del perímetro de cada vano,
 * dos postes de jamba y un canal de dintel por vano.
 */
class CalculatorEngineOpeningsTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    private fun WallCalculationResult.requirement(materialId: String): MaterialRequirement =
        requirements.first {
            it.material.id == materialId ||
            (materialId == "canal_guia_3m" && it.material.id.startsWith("canal_guia")) ||
            (materialId == "poste_montante_3m" && it.material.id.startsWith("poste_montante"))
        }

    @Test
    fun testUnVanoDescuentaPlacaAislamientoYSumaCintaDePerimetro() {
        // Muro 4.0 x 2.6 m a dos caras, con aislamiento y una puerta de 0.90 x 2.10 m.
        val input = WallCalculationInput(
            lengthMeters = 4.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = true,
            studSpacingMeters = 0.407,
            boardType = DrywallBoardType.ESTANDAR_ST,
            wasteFactor = 0.08,
            openings = listOf(
                WallOpening(
                    type = OpeningType.PUERTA,
                    label = "Puerta baño",
                    widthMeters = 0.9,
                    heightMeters = 2.1
                )
            )
        )

        val result = engine.calculate(input)

        // Superficies: bruta 10.40 m², vano 1.89 m², neta 8.51 m² por cara (17.02 m² a dos caras)
        assertEquals(10.4, result.input.wallAreaOneFace, 0.0001)
        assertEquals(1.89, result.openingsAreaM2, 0.0001)
        assertEquals(8.51, result.netAreaOneFace, 0.0001)
        assertEquals(17.02, result.netTotalDrywallArea, 0.0001)
        assertEquals(1, result.openingsCount)

        // Placas sobre el área neta: 17.02 / 2.9768 * 1.08 = 6.17 => 7 placas
        assertEquals(7, result.totalBoardsRequired)

        // Aislamiento sobre el área neta de una cara: 8.51 / 12 = 0.71 rollos base => 1 rollo
        val insulation = result.requirement("lana_aislamiento")
        assertEquals(1, insulation.commercialQuantity)
        assertEquals(8.51 / 12.0, insulation.rawQuantity, 0.0001)
        assertEquals(8.51 * 1.08 / 12.0, insulation.quantityWithWaste, 0.0001)

        // Cinta: 17.02 m² * 1.5 m/m² = 25.53 m + perímetro del vano 2*(0.90+2.10) = 6.00 m por 2 caras => 37.53 m
        val tape = result.requirement("cinta_juntas")
        assertEquals(37.53 / 75.0, tape.rawQuantity, 0.0001)
        assertTrue(tape.formulaExplanation.contains("perímetro de vanos"))
        assertTrue(tape.formulaExplanation.contains("37.5"))

        // Masilla sobre el área neta: 17.02 kg base * 1.08 = 18.38 kg => 1 balde de 28 kg
        val compound = result.requirement("masilla_juntas")
        assertEquals(17.02, compound.rawQuantity * 28.0, 0.0001)

        // Dintel: 1 canal por vano => 4.0*2 + 0.9 = 8.9 m * 1.08 / 3 = 3.2 => 4 piezas
        val track = result.requirement("canal_guia_3m")
        assertEquals(4, track.commercialQuantity)
        assertTrue(track.formulaExplanation.contains("dinteles de vano"))

        // Postes: 13 líneas de modulación + 2 jambas = 15 (con desperdicio) => 15 piezas
        val studs = result.requirement("poste_montante_3m")
        assertEquals(15, studs.commercialQuantity)
        assertTrue(studs.formulaExplanation.contains("postes de jamba"))

        // Tornillería coherente con los postes y placas resultantes
        assertEquals(13 * 4.0, result.requirement("tornillo_t1").rawQuantity, 0.0001)
        assertEquals(13 * 4.0 * 1.08, result.requirement("tornillo_t1").quantityWithWaste, 0.0001)
        assertEquals(57, result.requirement("tornillo_t1").commercialQuantity)
        assertEquals(7 * 30.0, result.requirement("tornillo_t2").rawQuantity, 0.0001)
        assertEquals(7 * 30.0 * 1.08, result.requirement("tornillo_t2").quantityWithWaste, 0.0001)
        assertEquals(227, result.requirement("tornillo_t2").commercialQuantity)

        // Anclajes solo sobre soleras (8.00 m / 0.50 + 2) * 1.08 = 19.44 => 20 unidades
        assertEquals(20, result.requirement("anclaje_chazo").commercialQuantity)
    }

    @Test
    fun testVariosVanosAcumulanDescuentoJambasYDinteles() {
        // Muro 5.0 x 2.6 m a dos caras con una puerta y una ventana.
        val input = WallCalculationInput(
            lengthMeters = 5.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = true,
            studSpacingMeters = 0.407,
            boardType = DrywallBoardType.ESTANDAR_ST,
            wasteFactor = 0.08,
            openings = listOf(
                WallOpening(OpeningType.PUERTA, "Puerta", 0.9, 2.1),
                WallOpening(OpeningType.VENTANA, "Ventana", 1.2, 1.0)
            )
        )

        val result = engine.calculate(input)

        assertEquals(3.09, result.openingsAreaM2, 0.0001)
        assertEquals(9.91, result.netAreaOneFace, 0.0001)
        assertEquals(19.82, result.netTotalDrywallArea, 0.0001)

        // Placas: 19.82 / 2.9768 * 1.08 = 7.19 => 8 placas
        assertEquals(8, result.totalBoardsRequired)

        // Canales: 10.0 m + dinteles 2.10 m = 12.10 m * 1.08 / 2.44 = 5.35 => 6 piezas
        assertEquals(6, result.requirement("canal_guia_3m").commercialQuantity)

        // Postes: 14 líneas de modulación + 4 jambas = 18 * 2.6m = 46.8m * 1.08 / 2.44 = 20.71 => 21 piezas
        assertEquals(21, result.requirement("poste_montante_3m").commercialQuantity)

        // Cinta: 19.82 * 1.5 = 29.73 m + (6.00 + 4.40) m de perímetros x 2 caras = 50.53 m
        assertEquals(50.53 / 75.0, result.requirement("cinta_juntas").rawQuantity, 0.0001)

        // Aislamiento: 9.91 * 1.08 / 12 = 0.89 => 1 rollo
        assertEquals(1, result.requirement("lana_aislamiento").commercialQuantity)

        // Masilla: 19.82 kg base => 1 balde de 28 kg
        assertEquals(19.82, result.requirement("masilla_juntas").rawQuantity * 28.0, 0.0001)

        // Anclajes: solo soleras (10.00 / 0.5) + 2 = 22.00 * 1.08 = 23.76 => 24 unidades
        assertEquals(24, result.requirement("anclaje_chazo").commercialQuantity)

        // Tornillería T2: 8 placas * 30 * 1.08 = 259.2 => 260 unidades
        assertEquals(260, result.requirement("tornillo_t2").commercialQuantity)
    }

    @Test
    fun testVanosQueExcedenElMuroSonErrorDeValidacionExplicito() {
        // Vano más alto y más ancho que el muro completo
        val input = WallCalculationInput(
            lengthMeters = 3.0,
            heightMeters = 2.4,
            openings = listOf(
                WallOpening(OpeningType.VENTANA, "Ventanal", 3.0, 2.6)
            )
        )

        val errors = input.validateOpenings()
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("excede el alto del muro") })

        // El motor nunca devuelve cantidades 0 en silencio: falla de forma explícita
        val exception = assertThrows(IllegalArgumentException::class.java) {
            engine.calculate(input)
        }
        assertTrue(exception.message!!.contains("sin superficie emplazable"))
    }

    @Test
    fun testVanosQueCubrenTodoElMuroSonErrorDeValidacion() {
        val input = WallCalculationInput(
            lengthMeters = 2.0,
            heightMeters = 1.0,
            openings = listOf(
                WallOpening(OpeningType.VENTANA, "Ventana total", 2.0, 1.0)
            )
        )

        assertTrue(input.validateOpenings().any { it.contains("sin superficie emplazable") })
        assertThrows(IllegalArgumentException::class.java) { engine.calculate(input) }
    }

    @Test
    fun testSinVanosElCalculoEsIdenticoAlComportamientoAnterior() {
        val conListaVacia = engine.calculate(
            WallCalculationInput(
                lengthMeters = 5.0,
                heightMeters = 2.6,
                faces = WallFaces.BOTH_FACES,
                hasInsulation = true,
                wasteFactor = 0.08,
                openings = emptyList()
            )
        )

        assertTrue(conListaVacia.input.validateOpenings().isEmpty())
        assertEquals(0, conListaVacia.openingsCount)
        assertEquals(conListaVacia.input.totalDrywallArea, conListaVacia.netTotalDrywallArea, 0.0001)
        assertEquals(10, conListaVacia.totalBoardsRequired)
        assertEquals(0.0, conListaVacia.openingsAreaM2, 0.0001)
    }

    @Test
    fun testViewModelGestionaVanosYBloqueaCalculosInvalidos() {
        val viewModel = DrywallCalculatorViewModel(engine)

        viewModel.onLengthChanged("4,0")
        viewModel.onHeightChanged("2,6")

        viewModel.addOpening()
        assertTrue(viewModel.uiState.value.openingsValidationErrors.isNotEmpty())
        assertNull(viewModel.uiState.value.calculationResult)

        // Medidas inválidas: ancho mayor que el largo del muro
        val openingId = viewModel.uiState.value.openingDrafts.first().id
        viewModel.updateOpeningWidth(openingId, "8,0")
        viewModel.updateOpeningHeight(openingId, "2,1")
        val invalidState = viewModel.uiState.value
        assertTrue(invalidState.openingsValidationErrors.any { it.contains("excede el largo del muro") })
        assertNull(invalidState.calculationResult)

        // Medidas válidas: el cálculo se recupera con el área neta descontada
        viewModel.updateOpeningWidth(openingId, "0,9")
        val validState = viewModel.uiState.value
        assertTrue(validState.openingsValidationErrors.isEmpty())
        assertNotNull(validState.calculationResult)
        assertEquals(1, validState.calculationResult?.openingsCount)
        assertEquals(8.51, validState.calculationResult?.netAreaOneFace ?: 0.0, 0.0001)

        // Quitar el vano devuelve el muro ciego
        viewModel.removeOpening(openingId)
        val clearedState = viewModel.uiState.value
        assertTrue(clearedState.openingDrafts.isEmpty())
        assertEquals(0, clearedState.calculationResult?.openingsCount)
        assertEquals(10.4, clearedState.calculationResult?.netAreaOneFace ?: 0.0, 0.0001)
    }

    @Test
    fun testReporteIncluyeSeccionDeVanosYAreaNeta() {
        val result = engine.calculate(
            WallCalculationInput(
                lengthMeters = 4.0,
                heightMeters = 2.6,
                faces = WallFaces.BOTH_FACES,
                hasInsulation = true,
                wasteFactor = 0.08,
                openings = listOf(
                    WallOpening(OpeningType.PUERTA, "Puerta baño", 0.9, 2.1)
                )
            )
        )

        val report = DrywallReportExporter.generateWhatsAppFormattedReport(
            result = result,
            projectName = "Obra de prueba"
        )

        assertTrue(report.contains("VANOS DE LA OBRA"))
        assertTrue(report.contains("Puerta baño"))
        assertTrue(report.contains("0.90 m × 2.10 m"))
        assertTrue(report.contains("17.02 m²"))
    }
}
