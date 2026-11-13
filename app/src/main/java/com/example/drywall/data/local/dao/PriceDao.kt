package com.example.drywall.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.drywall.data.local.entity.MaterialPriceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO exclusivo para operaciones con el catálogo de precios de materiales y mano de obra.
 */
@Dao
interface PriceDao {

    @Query("SELECT * FROM material_prices")
    fun observeMaterialPrices(): Flow<List<MaterialPriceEntity>>

    @Query("SELECT * FROM material_prices")
    suspend fun getMaterialPrices(): List<MaterialPriceEntity>

    @Upsert
    suspend fun upsertMaterialPrices(prices: List<MaterialPriceEntity>)
}
