package com.example.drywall.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar el cómputo métrico de un elemento (muro, cielo raso o mueble) dentro de un proyecto.
 * Incluye clave foránea a [ProjectEntity] con eliminación en cascada.
 *
 * Desde la versión 4 soporta múltiples tipos de elementos (`elementType`):
 * - MURO_DRYWALL
 * - CIELO_RASO_DRYWALL
 * - CIELO_RASO_PVC
 * - MUEBLE_DRYWALL
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
    val wallName: String = "Elemento Drywall",
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
    val paintFinishUnits: Int = 0,
    // --- Campos agregados en versión 4 para soportes de cielos rasos, PVC y muebles ---
    @ColumnInfo(defaultValue = "MURO_DRYWALL")
    val elementType: String = "MURO_DRYWALL",
    @ColumnInfo(defaultValue = "0.30")
    val suspensionHeightMeters: Double = 0.30,
    @ColumnInfo(defaultValue = "0.80")
    val mainBeamSpacingMeters: Double = 0.80,
    @ColumnInfo(defaultValue = "0.25")
    val slatWidthMeters: Double = 0.25,
    @ColumnInfo(defaultValue = "5.95")
    val commercialSlatLengthMeters: Double = 5.95,
    @ColumnInfo(defaultValue = "0")
    val horizontalShelvesCount: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val verticalDividersCount: Int = 0,
    @ColumnInfo(defaultValue = "0.0")
    val depthMeters: Double = 0.0
)

