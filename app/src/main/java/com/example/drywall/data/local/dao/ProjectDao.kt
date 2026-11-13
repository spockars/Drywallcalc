package com.example.drywall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.ProjectWithCalculations
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) exclusivo para operaciones de persistencia de Proyectos.
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

    @Query("UPDATE projects SET updatedAt = :updatedAt WHERE id = :projectId")
    suspend fun touchProject(projectId: Long, updatedAt: Long)

    @Query(
        "UPDATE projects SET laborEnabled = :enabled, laborMode = :mode, " +
            "laborAmount = :amount, updatedAt = :updatedAt WHERE id = :projectId"
    )
    suspend fun updateProjectLabor(
        projectId: Long,
        enabled: Boolean,
        mode: String,
        amount: Double,
        updatedAt: Long
    )

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}
