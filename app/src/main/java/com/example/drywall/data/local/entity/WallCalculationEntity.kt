package com.example.drywall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar el cómputo métrico de un muro dentro de un proyecto.
 * Incluye clave foránea a [ProjectEntity] con eliminación en cascada.
 */
@Entity(
    tableName = "wall_calculations",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class WallCalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val wallName: String = "Muro Drywall",
    val lengthMeters: Double,
    val heightMeters: Double,
    val faces: String, // ONE_FACE or BOTH_FACES
    val hasInsulation: Boolean,
    val studSpacingMeters: Double,
    val boardType: String,
    val wasteFactor: Double,
    val totalDrywallAreaM2: Double,
    val totalBoardsCount: Int,
    val totalProfiles3mCount: Int,
    val summaryNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
