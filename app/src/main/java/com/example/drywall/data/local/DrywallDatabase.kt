package com.example.drywall.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.WallCalculationEntity

/**
 * Base de datos SQLite local administrada mediante Room para DrywallCalc.
 */
@Database(
    entities = [
        ProjectEntity::class,
        WallCalculationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DrywallDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: DrywallDatabase? = null

        fun getInstance(context: Context): DrywallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrywallDatabase::class.java,
                    "drywall_calculator.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
