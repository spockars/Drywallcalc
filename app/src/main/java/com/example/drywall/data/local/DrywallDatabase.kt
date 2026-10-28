package com.example.drywall.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.drywall.data.local.dao.ProjectDao
import com.example.drywall.data.local.entity.MaterialPriceEntity
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.WallOpeningEntity

/**
 * Base de datos SQLite local administrada mediante Room para DrywallCalc.
 *
 * Versión 2: agrega los vanos (tabla `wall_openings`), el área neta emplazable y el
 * resultado de pintura por muro. No se usa migración destructiva: una base v1 existente
 * conserva sus obras y muros.
 */
@Database(
    entities = [
        ProjectEntity::class,
        WallCalculationEntity::class,
        WallOpeningEntity::class,
        MaterialPriceEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class DrywallDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {

        private const val DATABASE_NAME = "drywall_calculator.db"

        /**
         * Migración 1 → 2.
         *
         * Estrategia:
         * 1. Reconstruye `wall_calculations` con las columnas nuevas, copiando las filas v1.
         *    El área neta de las filas heredadas se iguala al área total: esos muros se
         *    calcularon sin vanos, así que bruta y neta coinciden.
         * 2. Crea `wall_openings` con su llave foránea en cascada y su índice.
         *
         * No se pierde ninguna fila: los datos de la versión 1 sobreviven íntegros.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wall_calculations_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`projectId` INTEGER NOT NULL, " +
                        "`wallName` TEXT NOT NULL, " +
                        "`lengthMeters` REAL NOT NULL, " +
                        "`heightMeters` REAL NOT NULL, " +
                        "`faces` TEXT NOT NULL, " +
                        "`hasInsulation` INTEGER NOT NULL, " +
                        "`studSpacingMeters` REAL NOT NULL, " +
                        "`boardType` TEXT NOT NULL, " +
                        "`wasteFactor` REAL NOT NULL, " +
                        "`totalDrywallAreaM2` REAL NOT NULL, " +
                        "`totalBoardsCount` INTEGER NOT NULL, " +
                        "`totalProfiles3mCount` INTEGER NOT NULL, " +
                        "`summaryNotes` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`netAreaM2` REAL NOT NULL, " +
                        "`paintEnabled` INTEGER NOT NULL, " +
                        "`paintFinishCoats` INTEGER NOT NULL, " +
                        "`paintWasteFactor` REAL NOT NULL, " +
                        "`paintProductName` TEXT NOT NULL, " +
                        "`paintUnit` TEXT NOT NULL, " +
                        "`paintCoverageM2PerUnit` REAL NOT NULL, " +
                        "`paintAreaM2` REAL NOT NULL, " +
                        "`paintSealerUnits` INTEGER NOT NULL, " +
                        "`paintFinishUnits` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                db.execSQL(
                    "INSERT INTO `wall_calculations_new` (" +
                        "`id`, `projectId`, `wallName`, `lengthMeters`, `heightMeters`, `faces`, " +
                        "`hasInsulation`, `studSpacingMeters`, `boardType`, `wasteFactor`, " +
                        "`totalDrywallAreaM2`, `totalBoardsCount`, `totalProfiles3mCount`, " +
                        "`summaryNotes`, `createdAt`, `netAreaM2`, `paintEnabled`, " +
                        "`paintFinishCoats`, `paintWasteFactor`, `paintProductName`, `paintUnit`, " +
                        "`paintCoverageM2PerUnit`, `paintAreaM2`, `paintSealerUnits`, `paintFinishUnits`) " +
                        "SELECT `id`, `projectId`, `wallName`, `lengthMeters`, `heightMeters`, `faces`, " +
                        "`hasInsulation`, `studSpacingMeters`, `boardType`, `wasteFactor`, " +
                        "`totalDrywallAreaM2`, `totalBoardsCount`, `totalProfiles3mCount`, " +
                        "`summaryNotes`, `createdAt`, `totalDrywallAreaM2`, 0, 2, 0.10, '', 'GALON', " +
                        "0.0, 0.0, 0, 0 FROM `wall_calculations`"
                )

                db.execSQL("DROP TABLE `wall_calculations`")
                db.execSQL("ALTER TABLE `wall_calculations_new` RENAME TO `wall_calculations`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_wall_calculations_projectId` " +
                        "ON `wall_calculations` (`projectId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wall_openings` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`wallId` INTEGER NOT NULL, " +
                        "`projectId` INTEGER NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`label` TEXT NOT NULL, " +
                        "`widthMeters` REAL NOT NULL, " +
                        "`heightMeters` REAL NOT NULL, " +
                        "FOREIGN KEY(`wallId`) REFERENCES `wall_calculations`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_wall_openings_wallId` " +
                        "ON `wall_openings` (`wallId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_wall_openings_projectId` " +
                        "ON `wall_openings` (`projectId`)"
                )
            }
        }

        /**
         * Migración 2 → 3.
         *
         * Agrega la configuración de mano de obra a cada obra (apagada por defecto) y crea el
         * catálogo de precios unitarios. Las obras, muros, vanos y pintura de la versión 2
         * quedan intactos: solo se agregan columnas con valor por defecto y una tabla nueva.
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `projects` ADD COLUMN `laborEnabled` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `projects` ADD COLUMN `laborMode` TEXT NOT NULL DEFAULT 'PER_M2'"
                )
                db.execSQL(
                    "ALTER TABLE `projects` ADD COLUMN `laborAmount` REAL NOT NULL DEFAULT 0.0"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `material_prices` (" +
                        "`itemKey` TEXT NOT NULL, " +
                        "`unitPrice` REAL NOT NULL, " +
                        "`unitLabel` TEXT NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`itemKey`))"
                )
            }
        }

        /**
         * Migración 3 → 4.
         *
         * Agrega soporte para múltiples tipos de elementos (`elementType`), parámetros para cielos rasos
         * de drywall y PVC, y parámetros para muebles/repisas de drywall.
         */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `elementType` TEXT NOT NULL DEFAULT 'MURO_DRYWALL'"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `suspensionHeightMeters` REAL NOT NULL DEFAULT 0.30"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `mainBeamSpacingMeters` REAL NOT NULL DEFAULT 0.80"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `slatWidthMeters` REAL NOT NULL DEFAULT 0.25"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `commercialSlatLengthMeters` REAL NOT NULL DEFAULT 5.95"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `horizontalShelvesCount` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `verticalDividersCount` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `wall_calculations` ADD COLUMN `depthMeters` REAL NOT NULL DEFAULT 0.0"
                )
            }
        }

        /** Todas las migraciones registradas, en orden. */
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

        @Volatile
        private var INSTANCE: DrywallDatabase? = null

        fun getInstance(context: Context): DrywallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrywallDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATIONS)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
