package com.example.drywall.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para representar un proyecto u obra de construcción liviana.
 *
 * Desde la versión 3 guarda la configuración de mano de obra de la obra: si está activa,
 * el modo (por m² o valor global) y el valor correspondiente.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val clientOrLocation: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    val laborEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "PER_M2")
    val laborMode: String = "PER_M2", // LaborMode.name
    @ColumnInfo(defaultValue = "0.0")
    val laborAmount: Double = 0.0
)
