package com.example.drywall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.WallOpeningEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO exclusivo para operaciones con muros, vanos y elementos de cómputo métrico en seco.
 */
@Dao
interface CalculationDao {

    @Query("SELECT * FROM wall_calculations WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getCalculationsByProjectId(projectId: Long): Flow<List<WallCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: WallCalculationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpenings(openings: List<WallOpeningEntity>): List<Long>

    @Query("SELECT * FROM wall_openings WHERE wallId = :wallId ORDER BY id ASC")
    suspend fun getOpeningsForWall(wallId: Long): List<WallOpeningEntity>

    @Query("DELETE FROM wall_openings WHERE wallId = :wallId")
    suspend fun deleteOpeningsByWallId(wallId: Long)

    @Query("DELETE FROM wall_calculations WHERE id = :calculationId")
    suspend fun deleteCalculationById(calculationId: Long)

    /**
     * Operación atómica: inserta un muro y sus vanos dentro de una única transacción.
     * Si ocurre algún error al insertar los vanos, la inserción del muro completo se cancela (rollback).
     */
    @Transaction
    suspend fun insertWallWithOpenings(
        calculation: WallCalculationEntity,
        openings: List<WallOpeningEntity>
    ): Long {
        val wallId = insertCalculation(calculation)
        if (openings.isNotEmpty()) {
            deleteOpeningsByWallId(wallId)
            val openingsWithWallId = openings.map { it.copy(wallId = wallId) }
            insertOpenings(openingsWithWallId)
        }
        return wallId
    }
}
