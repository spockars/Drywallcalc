package com.example.drywall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operaciones de persistencia de proyectos y cálculos métricos.
 */
@Dao
interface ProjectDao {

    @Transaction
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjectsWithCalculations(): Flow<List<ProjectWithCalculations>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithCalculationsById(projectId: Long): Flow<ProjectWithCalculations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: WallCalculationEntity): Long

    @Query("DELETE FROM wall_calculations WHERE id = :calculationId")
    suspend fun deleteCalculationById(calculationId: Long)

    @Query("SELECT * FROM wall_calculations WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getCalculationsByProjectId(projectId: Long): Flow<List<WallCalculationEntity>>

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}
