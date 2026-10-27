package com.example.drywall

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.entity.WallOpeningEntity
import com.example.drywall.data.local.entity.effectiveNetAreaM2
import com.example.drywall.data.local.entity.totalPaintUnits
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
import java.io.File

/**
 * Pruebas de la migración 1 → 2 de Room.
 *
 * Se crea un archivo SQLite con el esquema exacto de la versión 1, se insertan datos y luego
 * se abre con la base en versión 2 aplicando [DrywallDatabase.MIGRATIONS]. La prueba falla si
 * Room detecta un esquema distinto o si se pierde alguna fila.
 */
@RunWith(RobolectricTestRunner::class)
class DrywallRoomMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-v1-v2.db"

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    /** Crea el archivo con el DDL de la versión 1 tal como lo generaba Room. */
    private fun createVersion1Database(): File {
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()

        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `projects` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `clientOrLocation` TEXT NOT NULL, `notes` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wall_calculations` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`projectId` INTEGER NOT NULL, `wallName` TEXT NOT NULL, " +
                "`lengthMeters` REAL NOT NULL, `heightMeters` REAL NOT NULL, `faces` TEXT NOT NULL, " +
                "`hasInsulation` INTEGER NOT NULL, `studSpacingMeters` REAL NOT NULL, " +
                "`boardType` TEXT NOT NULL, `wasteFactor` REAL NOT NULL, " +
                "`totalDrywallAreaM2` REAL NOT NULL, `totalBoardsCount` INTEGER NOT NULL, " +
                "`totalProfiles3mCount` INTEGER NOT NULL, `summaryNotes` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_wall_calculations_projectId` " +
                "ON `wall_calculations` (`projectId`)"
        )
        db.execSQL(
            "INSERT INTO `projects` (`id`, `name`, `clientOrLocation`, `notes`, `createdAt`, `updatedAt`) " +
                "VALUES (1, 'Obra v1', 'Cliente v1', 'Nota heredada', 1000, 1000)"
        )
        db.execSQL(
            "INSERT INTO `wall_calculations` (`id`, `projectId`, `wallName`, `lengthMeters`, " +
                "`heightMeters`, `faces`, `hasInsulation`, `studSpacingMeters`, `boardType`, " +
                "`wasteFactor`, `totalDrywallAreaM2`, `totalBoardsCount`, `totalProfiles3mCount`, " +
                "`summaryNotes`, `createdAt`) VALUES " +
                "(1, 1, 'Muro v1', 5.0, 2.6, 'BOTH_FACES', 1, 0.407, 'ESTANDAR_ST', 0.08, " +
                "26.0, 10, 20, 'Placas: 10 unid', 1000)"
        )
        db.version = 1
        db.close()
        return file
    }

    @Test
    fun testLosDatosDeUnaBaseV1SobrevivenALaMigracion() = runBlocking {
        createVersion1Database()

        val database = Room.databaseBuilder(context, DrywallDatabase::class.java, databaseName)
            .addMigrations(*DrywallDatabase.MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            val projects = database.projectDao().getAllProjectsWithCalculations().first()

            // La obra y su muro siguen ahí, con los mismos valores
            assertEquals(1, projects.size)
            val project = projects[0]
            assertEquals("Obra v1", project.project.name)
            assertEquals("Cliente v1", project.project.clientOrLocation)
            assertEquals(1, project.calculations.size)

            val wall = project.calculations[0]
            assertEquals("Muro v1", wall.wallName)
            assertEquals(5.0, wall.lengthMeters, 0.0001)
            assertEquals(2.6, wall.heightMeters, 0.0001)
            assertEquals(26.0, wall.totalDrywallAreaM2, 0.0001)
            assertEquals(10, wall.totalBoardsCount)
            assertEquals(20, wall.totalProfiles3mCount)
            assertTrue(wall.hasInsulation)

            // Columnas nuevas: el área neta heredada se rellena con el área total (no había vanos)
            assertEquals(26.0, wall.netAreaM2, 0.0001)
            assertEquals(26.0, wall.effectiveNetAreaM2, 0.0001)

            // Defaults de pintura: apagada y sin envases
            assertFalse(wall.paintEnabled)
            assertEquals(2, wall.paintFinishCoats)
            assertEquals(0, wall.paintSealerUnits)
            assertEquals(0, wall.totalPaintUnits)

            // La tabla de vanos existe, acepta insertos y la relación los devuelve
            assertTrue(project.openings.isEmpty())
            database.projectDao().insertOpenings(
                listOf(
                    WallOpeningEntity(
                        wallId = wall.id,
                        projectId = project.project.id,
                        type = "PUERTA",
                        label = "Puerta heredada",
                        widthMeters = 0.9,
                        heightMeters = 2.1
                    )
                )
            )

            val reloaded = database.projectDao().getAllProjectsWithCalculations().first().first()
            assertEquals(1, reloaded.openings.size)
            assertEquals(1, reloaded.totalOpenings)
            assertEquals("Puerta heredada", reloaded.openingsFor(wall.id).first().label)
        } finally {
            database.close()
        }
    }

    @Test
    fun testLaMigracion1a2EstaRegistrada() {
        val ranges = DrywallDatabase.MIGRATIONS.map { it.startVersion to it.endVersion }
        assertTrue("Debe existir una Migration(1, 2) registrada en MIGRATIONS", ranges.contains(1 to 2))
    }
}
