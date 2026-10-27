package com.example.drywall.data.repository

import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.mapper.toEntity
import com.example.drywall.data.mapper.toOpeningEntities
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
 *
 * Cada muro se guarda con sus vanos y su resultado de pintura, y el proyecto se marca como
 * modificado para que la lista de obras refleje el último cambio.
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
            name = projectName.ifBlank { "Proyecto $now" },
            clientOrLocation = clientOrLocation,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        val projectId = projectDao.insertProject(project)
        insertWall(
            projectId = projectId,
            wallName = wallName.ifBlank { "Muro 1" },
            calculationResult = calculationResult,
            timestamp = now
        )
        return projectId
    }

    override suspend fun addCalculationToProject(
        projectId: Long,
        wallName: String,
        calculationResult: WallCalculationResult
    ): Long {
        val now = System.currentTimeMillis()
        val wallId = insertWall(
            projectId = projectId,
            wallName = wallName.ifBlank { "Muro Adicional" },
            calculationResult = calculationResult,
            timestamp = now
        )
        projectDao.touchProject(projectId, now)
        return wallId
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

    private suspend fun insertWall(
        projectId: Long,
        wallName: String,
        calculationResult: WallCalculationResult,
        timestamp: Long
    ): Long {
        val wallId = projectDao.insertCalculation(
            calculationResult.toEntity(
                projectId = projectId,
                wallName = wallName,
                createdAt = timestamp
            )
        )
        val openings = calculationResult.toOpeningEntities(wallId = wallId, projectId = projectId)
        if (openings.isNotEmpty()) {
            projectDao.insertOpenings(openings)
        }
        return wallId
    }
}
