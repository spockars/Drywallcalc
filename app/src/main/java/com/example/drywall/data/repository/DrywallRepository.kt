package com.example.drywall.data.repository

import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.MaterialPriceEntity
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.mapper.toEntity
import com.example.drywall.data.mapper.toOpeningEntities
import com.example.drywall.data.mapper.toDomain
import com.example.drywall.domain.model.PriceEntry
import com.example.drywall.domain.model.ProjectLabor
import com.example.drywall.domain.model.WallCalculationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para abstraer el acceso a datos locales de proyectos y cómputos de Drywall.
 */
interface DrywallRepository {
    val allProjectsWithCalculations: Flow<List<ProjectWithCalculations>>
    fun getProjectById(projectId: Long): Flow<ProjectWithCalculations?>
    suspend fun createEmptyProject(
        projectName: String,
        clientOrLocation: String = "",
        notes: String = ""
    ): Long
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
    suspend fun addCalculationEntityToProject(
        entity: WallCalculationEntity
    ): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(projectId: Long)
    suspend fun deleteCalculation(calculationId: Long)

    /** Catálogo de precios unitarios vigente. */
    val materialPrices: Flow<List<PriceEntry>>

    /** Guarda (o actualiza) precios unitarios. */
    suspend fun saveMaterialPrices(prices: List<PriceEntry>)

    /** Actualiza la mano de obra de una obra. */
    suspend fun updateProjectLabor(projectId: Long, labor: ProjectLabor)
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

    override suspend fun createEmptyProject(
        projectName: String,
        clientOrLocation: String,
        notes: String
    ): Long {
        val now = System.currentTimeMillis()
        val project = ProjectEntity(
            name = projectName.ifBlank { "Obra $now" },
            clientOrLocation = clientOrLocation,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        return projectDao.insertProject(project)
    }

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
            wallName = wallName.ifBlank { "Elemento 1" },
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
            wallName = wallName.ifBlank { "Elemento Adicional" },
            calculationResult = calculationResult,
            timestamp = now
        )
        projectDao.touchProject(projectId, now)
        return wallId
    }

    override suspend fun addCalculationEntityToProject(entity: WallCalculationEntity): Long {
        val now = System.currentTimeMillis()
        val id = projectDao.insertCalculation(entity.copy(createdAt = now))
        projectDao.touchProject(entity.projectId, now)
        return id
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

    override val materialPrices: Flow<List<PriceEntry>> =
        projectDao.observeMaterialPrices().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveMaterialPrices(prices: List<PriceEntry>) {
        if (prices.isEmpty()) return
        val now = System.currentTimeMillis()
        projectDao.upsertMaterialPrices(
            prices.map { it.copy(updatedAt = now).toEntity() }
        )
    }

    override suspend fun updateProjectLabor(projectId: Long, labor: ProjectLabor) {
        projectDao.updateProjectLabor(
            projectId = projectId,
            enabled = labor.enabled,
            mode = labor.mode.name,
            amount = labor.amount,
            updatedAt = System.currentTimeMillis()
        )
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
