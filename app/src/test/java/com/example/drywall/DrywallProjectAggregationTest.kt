package com.example.drywall

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.mapper.toInput
import com.example.drywall.data.mapper.toPaintEstimate
import com.example.drywall.data.repository.DrywallRepository
import com.example.drywall.data.repository.DrywallRepositoryImpl
import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.engine.PaintCalculator
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pruebas de la obra real: agregación multi-muro, pintura persistida y recarga exacta.
 */
@RunWith(RobolectricTestRunner::class)
class DrywallProjectAggregationTest {

    private lateinit var database: DrywallDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var repository: DrywallRepository
    private val engine = CalculatorEngine()

    private val paintSpec = PaintSpec(
        finishCoats = 2,
        wasteFactor = 0.10,
        product = PaintProduct(name = "Acrílica tipo 1", unit = PaintUnit.GALON, coverageM2PerUnit = 10.0)
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DrywallDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        projectDao = database.projectDao()
        repository = DrywallRepositoryImpl(projectDao)
    }

    @After
    fun closeDb() {
        database.close()
    }

    private fun calculate(
        length: Double,
        height: Double,
        openings: List<WallOpening> = emptyList(),
        withPaint: Boolean = false
    ): WallCalculationResult {
        val result = engine.calculate(
            WallCalculationInput(
                lengthMeters = length,
                heightMeters = height,
                faces = WallFaces.BOTH_FACES,
                hasInsulation = true,
                studSpacingMeters = 0.407,
                boardType = DrywallBoardType.ESTANDAR_ST,
                wasteFactor = 0.08,
                openings = openings
            )
        )
        return if (withPaint) {
            result.copy(paint = PaintCalculator.estimate(result.netTotalDrywallArea, paintSpec))
        } else {
            result
        }
    }

    @Test
    fun testAgregacionDeVariosMurosConVanosYPintura() = runBlocking {
        val muro1 = calculate(
            length = 5.0,
            height = 2.6,
            openings = listOf(WallOpening(OpeningType.PUERTA, "Puerta", 0.9, 2.1)),
            withPaint = true
        )
        val muro2 = calculate(length = 3.0, height = 2.4)

        val projectId = repository.saveProjectWithCalculation(
            projectName = "Obra Multipiso",
            clientOrLocation = "Constructora",
            notes = "",
            calculationResult = muro1,
            wallName = "Muro 1"
        )
        repository.addCalculationToProject(projectId, "Muro 2", muro2)

        val project = repository.allProjectsWithCalculations.first().first()

        assertEquals(2, project.calculations.size)
        assertEquals(1, project.openings.size) // solo el muro 1 tiene puerta
        assertEquals(1, project.totalOpenings)

        // Área neta agregada (no el área bruta)
        val expectedNet = muro1.netTotalDrywallArea + muro2.netTotalDrywallArea
        assertEquals(expectedNet, project.totalNetAreaM2, 0.0001)
        assertTrue(project.totalNetAreaM2 < muro1.input.totalDrywallArea + muro2.input.totalDrywallArea)

        assertEquals(muro1.totalBoardsRequired + muro2.totalBoardsRequired, project.totalBoards)
        assertEquals(muro1.totalProfiles3mRequired + muro2.totalProfiles3mRequired, project.totalProfiles3m)

        // Pintura agregada: solo el muro 1 la tiene configurada
        assertEquals(muro1.paint!!.totalUnits, project.totalPaintUnits)
    }

    @Test
    fun testRecargarUnMuroReproduceElCalculoConVanosYPintura() = runBlocking {
        val original = calculate(
            length = 4.0,
            height = 2.6,
            openings = listOf(WallOpening(OpeningType.PUERTA, "Baño", 0.9, 2.1)),
            withPaint = true
        )

        val projectId = repository.saveProjectWithCalculation(
            projectName = "Obra Recarga",
            clientOrLocation = "",
            notes = "",
            calculationResult = original,
            wallName = "Muro baño"
        )

        val project = repository.allProjectsWithCalculations.first().first()
        val wall = project.calculations.first()
        val reloaded = project.toWallWithOpenings(wall)

        // El vano se persistió y la entrada reconstruida es idéntica
        assertEquals(1, reloaded.openings.size)
        val input = wall.toInput(reloaded.openings)
        assertEquals(1, input.openings.size)
        assertEquals(0.9, input.openings.first().widthMeters, 0.0001)

        val recalculated = engine.calculate(input)
        assertEquals(original.totalBoardsRequired, recalculated.totalBoardsRequired)
        assertEquals(original.netTotalDrywallArea, recalculated.netTotalDrywallArea, 0.0001)
        assertEquals(original.openingsAreaM2, recalculated.openingsAreaM2, 0.0001)
        assertEquals(original.totalProfiles3mRequired, recalculated.totalProfiles3mRequired)
        assertEquals(1, recalculated.openingsCount)

        // La pintura persistida se reconstruye con los mismos envases
        val paint = wall.toPaintEstimate()
        assertNotNull(paint)
        assertEquals(original.paint!!.totalUnits, paint!!.totalUnits)
        assertEquals(original.paint!!.areaM2, paint.areaM2, 0.0001)
        assertEquals(original.paint!!.sealerUnits, paint.sealerUnits)
        assertEquals(original.paint!!.finishUnits, paint.finishUnits)
        assertEquals("Acrílica tipo 1", paint.productName)
    }

    @Test
    fun testBorrarUnMuroSueltoConservaLaObraYLosDemasMuros() = runBlocking {
        val muro1 = calculate(
            length = 5.0,
            height = 2.6,
            openings = listOf(WallOpening(OpeningType.VENTANA, "Ventana", 1.2, 1.0))
        )
        val muro2 = calculate(length = 3.0, height = 2.4)

        val projectId = repository.saveProjectWithCalculation(
            projectName = "Obra con dos muros",
            clientOrLocation = "",
            notes = "",
            calculationResult = muro1,
            wallName = "Muro ventana"
        )
        repository.addCalculationToProject(projectId, "Muro liso", muro2)

        val before = repository.allProjectsWithCalculations.first().first()
        assertEquals(2, before.calculations.size)
        assertEquals(1, before.openings.size)

        val wallWithOpenings = before.calculations.first { it.wallName == "Muro ventana" }
        repository.deleteCalculation(wallWithOpenings.id)

        val after = repository.allProjectsWithCalculations.first().first()
        assertEquals(1, after.calculations.size)
        assertEquals("Muro liso", after.calculations.first().wallName)
        // Los vanos del muro borrado se fueron en cascada
        assertTrue(after.openings.isEmpty())
        assertEquals(0, after.totalOpenings)
    }

    @Test
    fun testMuroSinPinturaNoReportaEnvases() = runBlocking {
        val muro = calculate(length = 4.0, height = 2.6)

        repository.saveProjectWithCalculation(
            projectName = "Obra sin pintura",
            clientOrLocation = "",
            notes = "",
            calculationResult = muro,
            wallName = "Muro"
        )

        val project = repository.allProjectsWithCalculations.first().first()
        val wall = project.calculations.first()

        assertEquals(0, project.totalPaintUnits)
        assertNull(wall.toPaintEstimate())
        assertTrue(!wall.paintEnabled)
    }
}
