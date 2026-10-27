package com.example.drywall

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.repository.DrywallRepository
import com.example.drywall.data.repository.DrywallRepositoryImpl
import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallFaces
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pruebas locales de integración para la persistencia Room Database de proyectos y cómputos.
 */
@RunWith(RobolectricTestRunner::class)
class DrywallRoomPersistenceTest {

    private lateinit var database: DrywallDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var repository: DrywallRepository
    private val engine = CalculatorEngine()

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

    @Test
    fun testInsertAndRetrieveProjectWithCalculations() = runBlocking {
        val calcInput = WallCalculationInput(
            lengthMeters = 5.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = true
        )
        val calcResult = engine.calculate(calcInput)

        // 1. Guardar mediante el Repositorio
        val projectId = repository.saveProjectWithCalculation(
            projectName = "Edificio Torre Central - Apto 302",
            clientOrLocation = "Constructora Hábitat",
            notes = "Instalación de tabiquería con placa RH y lana de vidrio",
            calculationResult = calcResult,
            wallName = "Muro Divisorio Cocina"
        )

        assertTrue(projectId > 0)

        // 2. Recuperar todos los proyectos de la base de datos
        val allProjects = repository.allProjectsWithCalculations.first()
        assertEquals(1, allProjects.size)

        val savedProject = allProjects[0]
        assertEquals("Edificio Torre Central - Apto 302", savedProject.project.name)
        assertEquals("Constructora Hábitat", savedProject.project.clientOrLocation)
        assertEquals(1, savedProject.calculations.size)

        val savedWall = savedProject.calculations[0]
        assertEquals("Muro Divisorio Cocina", savedWall.wallName)
        assertEquals(5.0, savedWall.lengthMeters, 0.001)
        assertEquals(2.6, savedWall.heightMeters, 0.001)
        assertEquals(26.0, savedWall.totalDrywallAreaM2, 0.001)
        assertEquals(10, savedWall.totalBoardsCount)
        assertEquals(true, savedWall.hasInsulation)
    }

    @Test
    fun testCascadeDeletionOfProjectCalculations() = runBlocking {
        val calcResult = engine.calculate(
            lengthMeters = 4.0,
            heightMeters = 2.5,
            isTwoFaces = true
        )

        val projectId = repository.saveProjectWithCalculation(
            projectName = "Proyecto Temporal",
            clientOrLocation = "Oficina",
            notes = "",
            calculationResult = calcResult
        )

        val initialProjects = repository.allProjectsWithCalculations.first()
        assertEquals(1, initialProjects.size)

        // Eliminar el proyecto
        repository.deleteProject(projectId)

        val updatedProjects = repository.allProjectsWithCalculations.first()
        assertEquals(0, updatedProjects.size)

        // Verificar que los cómputos hijos se eliminaron en cascada
        val remainingCalculations = projectDao.getCalculationsByProjectId(projectId).first()
        assertTrue(remainingCalculations.isEmpty())
    }
}
