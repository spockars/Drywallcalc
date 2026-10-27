package com.example.drywall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar el cómputo métrico de un muro dentro de un proyecto.
 * Incluye clave foránea a [ProjectEntity] con eliminación en cascada.
 *
 * Desde la versión 2 de la base guarda además el área neta emplazable (descontando vanos)
 * y el resultado de pintura del muro, para que al recargar el proyecto el cálculo se
 * reproduzca exactamente igual.
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
    /** Área geométrica bruta de todas las caras, sin descontar vanos. */
    val totalDrywallAreaM2: Double,
    val totalBoardsCount: Int,
    val totalProfiles3mCount: Int,
    val summaryNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    /** Área neta emplazable de todas las caras (bruta menos vanos). */
    val netAreaM2: Double = 0.0,
    // --- Pintura del muro (calculada fuera del motor de drywall) ---
    val paintEnabled: Boolean = false,
    val paintFinishCoats: Int = 2,
    val paintWasteFactor: Double = 0.10,
    val paintProductName: String = "",
    val paintUnit: String = "GALON",
    val paintCoverageM2PerUnit: Double = 0.0,
    val paintAreaM2: Double = 0.0,
    val paintSealerUnits: Int = 0,
    val paintFinishUnits: Int = 0
)
