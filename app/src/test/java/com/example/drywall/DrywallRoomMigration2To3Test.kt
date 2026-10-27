package com.example.drywall

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.entity.MaterialPriceEntity
import com.example.drywall.data.local.entity.effectiveNetAreaM2
import com.example.drywall.data.local.entity.totalPaintUnits
import com.example.drywall.data.mapper.toDomain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pruebas de la migración 2 → 3.
 *
 * Se arma un archivo con el esquema exacto de la versión 2 (dos obras guardadas, un muro con
 * vano y pintura), se abre con la base en versión 3 y se verifica que nada se pierda, que la
 * mano de obra llegue apagada por defecto y que el catálogo de precios quede operativo.
 */
@RunWith(RobolectricTestRunner::class)
class DrywallRoomMigration2To3Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-v2-v3.db"

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    private fun createVersion2Database() {
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()

        val db = SQLiteDatabase.openOrCreateDatabase(file, null)

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `projects` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`clientOrLocation` TEXT NOT NULL, `notes` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wall_calculations` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, " +
                "`wallName` TEXT NOT NULL, `lengthMeters` REAL NOT NULL, `heightMeters` REAL NOT NULL, " +
                "`faces` TEXT NOT NULL, `hasInsulation` INTEGER NOT NULL, " +
                "`studSpacingMeters` REAL NOT NULL, `boardType` TEXT NOT NULL, " +
                "`wasteFactor` REAL NOT NULL, `totalDrywallAreaM2` REAL NOT NULL, " +
                "`totalBoardsCount` INTEGER NOT NULL, `totalProfiles3mCount` INTEGER NOT NULL, " +
                "`summaryNotes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`netAreaM2` REAL NOT NULL, `paintEnabled` INTEGER NOT NULL, " +
                "`paintFinishCoats` INTEGER NOT NULL, `paintWasteFactor` REAL NOT NULL, " +
                "`paintProductName` TEXT NOT NULL, `paintUnit` TEXT NOT NULL, " +
                "`paintCoverageM2PerUnit` REAL NOT NULL, `paintAreaM2` REAL NOT NULL, " +
                "`paintSealerUnits` INTEGER NOT NULL, `paintFinishUnits` INTEGER NOT NULL, " +
                "FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wall_openings` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wallId` INTEGER NOT NULL, " +
                "`projectId` INTEGER NOT NULL, `type` TEXT NOT NULL, `label` TEXT NOT NULL, " +
                "`widthMeters` REAL NOT NULL, `heightMeters` REAL NOT NULL, " +
                "FOREIGN KEY(`wallId`) REFERENCES `wall_calculations`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_calculations_projectId` ON `wall_calculations` (`projectId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_openings_wallId` ON `wall_openings` (`wallId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_openings_projectId` ON `wall_openings` (`projectId`)")

        // Dos obras guardadas, como en el dispositivo real
        db.execSQL(
            "INSERT INTO `projects` (`id`,`name`,`clientOrLocation`,`notes`,`createdAt`,`updatedAt`) VALUES " +
                "(1,'Obra Uno','Cliente Uno','nota uno',1000,2000), " +
                "(2,'Obra Dos','Cliente Dos','nota dos',1500,2500)"
        )
        // Muro de la obra 1, con vano y pintura
        db.execSQL(
            "INSERT INTO `wall_calculations` (`id`,`projectId`,`wallName`,`lengthMeters`,`heightMeters`," +
                "`faces`,`hasInsulation`,`studSpacingMeters`,`boardType`,`wasteFactor`,`totalDrywallAreaM2`," +
                "`totalBoardsCount`,`totalProfiles3mCount`,`summaryNotes`,`createdAt`,`netAreaM2`,`paintEnabled`," +
                "`paintFinishCoats`,`paintWasteFactor`,`paintProductName`,`paintUnit`,`paintCoverageM2PerUnit`," +
                "`paintAreaM2`,`paintSealerUnits`,`paintFinishUnits`) VALUES " +
                "(1,1,'Muro con puerta',4.0,2.6,'BOTH_FACES',1,0.407,'ESTANDAR_ST',0.08,20.8,7,19," +
                "'Placas: 7 unid',1200,17.02,1,2,0.1,'Acrílica','GALON',10.0,17.02,3,6)"
        )
        // Muro de la obra 2, sin vanos ni pintura
        db.execSQL(
            "INSERT INTO `wall_calculations` (`id`,`projectId`,`wallName`,`lengthMeters`,`heightMeters`," +
                "`faces`,`hasInsulation`,`studSpacingMeters`,`boardType`,`wasteFactor`,`totalDrywallAreaM2`," +
                "`totalBoardsCount`,`totalProfiles3mCount`,`summaryNotes`,`createdAt`,`netAreaM2`,`paintEnabled`," +
                "`paintFinishCoats`,`paintWasteFactor`,`paintProductName`,`paintUnit`,`paintCoverageM2PerUnit`," +
                "`paintAreaM2`,`paintSealerUnits`,`paintFinishUnits`) VALUES " +
                "(2,2,'Muro liso',3.0,2.4,'ONE_FACE',0,0.407,'ESTANDAR_ST',0.08,7.2,3,11," +
                "'Placas: 3 unid',1300,7.2,0,2,0.1,'','GALON',0.0,0.0,0,0)"
        )
        db.execSQL(
            "INSERT INTO `wall_openings` (`id`,`wallId`,`projectId`,`type`,`label`,`widthMeters`,`heightMeters`) " +
                "VALUES (1,1,1,'PUERTA','Puerta baño',0.9,2.1)"
        )

        db.version = 2
        db.close()
    }

    @Test
    fun testLosDatosDeLaVersion2SobrevivenYSeAgregaElCatalogo() = runBlocking {
        createVersion2Database()

        val database = Room.databaseBuilder(context, DrywallDatabase::class.java, databaseName)
            .addMigrations(*DrywallDatabase.MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            val projects = database.projectDao().getAllProjectsWithCalculations().first()

            // Las dos obras siguen ahí, con sus muros
            assertEquals(2, projects.size)
            val obraUno = projects.first { it.project.name == "Obra Uno" }
            val obraDos = projects.first { it.project.name == "Obra Dos" }

            assertEquals(1, obraUno.calculations.size)
            assertEquals("Muro con puerta", obraUno.calculations.first().wallName)
            assertEquals(1, obraUno.openings.size)
            assertEquals("Puerta baño", obraUno.openings.first().label)

            assertEquals(1, obraDos.calculations.size)
            assertEquals("Muro liso", obraDos.calculations.first().wallName)
            assertTrue(obraDos.openings.isEmpty())

            // Área neta y pintura intactas
            assertEquals(17.02, obraUno.calculations.first().effectiveNetAreaM2, 0.0001)
            assertEquals(9, obraUno.calculations.first().totalPaintUnits)

            // Mano de obra: llega apagada y sin valor, nunca inventada
            assertFalse(obraUno.project.laborEnabled)
            assertEquals("PER_M2", obraUno.project.laborMode)
            assertEquals(0.0, obraUno.project.laborAmount, 0.0001)
            assertFalse(obraDos.project.laborEnabled)

            // El catálogo de precios existe, arranca vacío y ya se puede guardar
            assertTrue(database.projectDao().getMaterialPrices().isEmpty())
            database.projectDao().upsertMaterialPrices(
                listOf(
                    MaterialPriceEntity(
                        itemKey = "placa_st_12_7",
                        unitPrice = 62_000.0,
                        unitLabel = "placa (1.22 x 2.44 m)",
                        updatedAt = 5000
                    )
                )
            )
            val prices = database.projectDao().getMaterialPrices()
            assertEquals(1, prices.size)
            assertEquals(62_000.0, prices.first().toDomain().unitPrice, 0.01)
            assertEquals("placa (1.22 x 2.44 m)", prices.first().unitLabel)
        } finally {
            database.close()
        }
    }

    @Test
    fun testLaCadenaDeMigracionesIncluye1a2y2a3() {
        val ranges = DrywallDatabase.MIGRATIONS.map { it.startVersion to it.endVersion }
        assertTrue(ranges.contains(1 to 2))
        assertTrue(ranges.contains(2 to 3))
    }
}
