package com.example.drywall.data.repository

import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.domain.model.WallCalculationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para abstraer el acceso a datos locales de proyectos y cómputos de Drywall.
 */
interface DrywallRepository {
    val allProjectsWithCalculations: Flow<List<ProjectWithCalculations>>
    fun getProjectById(projectId: Long): Flow<ProjectWithCalculations?>
    suspend fun saveProjectWithCalculation(
        projectName: String,
        clientOrLocation: String,
        notes: String,
        calculationResult: WallCalculationResult,
        wallName: String = "Muro Principal"
    ): Long
    suspend fun addCalculationToProject(
        projectId: Long,
        wallName: String,
        calculationResult: WallCalculationResult
    ): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(projectId: Long)
    suspend fun deleteCalculation(calculationId: Long)
}

/**
 * Implementación concreta del repositorio utilizando [ProjectDao].
 */
class DrywallRepositoryImpl(
    private val projectDao: ProjectDao
) : DrywallRepository {

    override val allProjectsWithCalculations: Flow<List<ProjectWithCalculations>> =
        projectDao.getAllProjectsWithCalculations()

    override fun getProjectById(projectId: Long): Flow<ProjectWithCalculations?> =
        projectDao.getProjectWithCalculationsById(projectId)

    override suspend fun saveProjectWithCalculation(
        projectName: String,
        clientOrLocation: String,
        notes: String,
        calculationResult: WallCalculationResult,
        wallName: String
    ): Long {
        val now = System.currentTimeMillis()
        val project = ProjectEntity(
            name = projectName.ifBlank { "Proyecto ${now}" },
            clientOrLocation = clientOrLocation,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        val projectId = projectDao.insertProject(project)

        val calculationEntity = WallCalculationEntity(
            projectId = projectId,
            wallName = wallName.ifBlank { "Muro 1" },
            lengthMeters = calculationResult.input.lengthMeters,
            heightMeters = calculationResult.input.heightMeters,
            faces = calculationResult.input.faces.name,
            hasInsulation = calculationResult.input.hasInsulation,
            studSpacingMeters = calculationResult.input.studSpacingMeters,
            boardType = calculationResult.input.boardType.name,
            wasteFactor = calculationResult.input.wasteFactor,
            totalDrywallAreaM2 = calculationResult.input.totalDrywallArea,
            totalBoardsCount = calculationResult.totalBoardsRequired,
            totalProfiles3mCount = calculationResult.totalProfiles3mRequired,
            summaryNotes = "Placas: ${calculationResult.totalBoardsRequired} unid, Perfiles 3m: ${calculationResult.totalProfiles3mRequired} unid",
            createdAt = now
        )
        projectDao.insertCalculation(calculationEntity)

        return projectId
    }

    override suspend fun addCalculationToProject(
        projectId: Long,
        wallName: String,
        calculationResult: WallCalculationResult
    ): Long {
        val now = System.currentTimeMillis()
        val calculationEntity = WallCalculationEntity(
            projectId = projectId,
            wallName = wallName.ifBlank { "Muro Adicional" },
            lengthMeters = calculationResult.input.lengthMeters,
            heightMeters = calculationResult.input.heightMeters,
            faces = calculationResult.input.faces.name,
            hasInsulation = calculationResult.input.hasInsulation,
            studSpacingMeters = calculationResult.input.studSpacingMeters,
            boardType = calculationResult.input.boardType.name,
            wasteFactor = calculationResult.input.wasteFactor,
            totalDrywallAreaM2 = calculationResult.input.totalDrywallArea,
            totalBoardsCount = calculationResult.totalBoardsRequired,
            totalProfiles3mCount = calculationResult.totalProfiles3mRequired,
            summaryNotes = "Placas: ${calculationResult.totalBoardsRequired} unid, Perfiles 3m: ${calculationResult.totalProfiles3mRequired} unid",
            createdAt = now
        )
        return projectDao.insertCalculation(calculationEntity)
    }

    override suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteProject(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    override suspend fun deleteCalculation(calculationId: Long) {
        projectDao.deleteCalculationById(calculationId)
    }
}
