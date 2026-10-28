package com.example.drywall

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.drywall.data.local.DrywallDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DrywallRoomMigration3To4Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-v3-v4.db"

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    private fun createVersion3Database() {
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()

        val db = SQLiteDatabase.openOrCreateDatabase(file, null)

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `projects` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`clientOrLocation` TEXT NOT NULL, `notes` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "`laborEnabled` INTEGER NOT NULL DEFAULT 0, `laborMode` TEXT NOT NULL DEFAULT 'PER_M2', " +
                "`laborAmount` REAL NOT NULL DEFAULT 0.0)"
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
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_calculations_projectId` ON `wall_calculations` (`projectId`)")
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
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_openings_wallId` ON `wall_openings` (`wallId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wall_openings_projectId` ON `wall_openings` (`projectId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `material_prices` (" +
                "`itemKey` TEXT NOT NULL, `unitPrice` REAL NOT NULL, " +
                "`unitLabel` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`itemKey`))"
        )

        db.execSQL(
            "INSERT INTO `projects` (`id`,`name`,`clientOrLocation`,`notes`,`createdAt`,`updatedAt`,`laborEnabled`,`laborMode`,`laborAmount`) VALUES " +
                "(1,'Obra V3','Cliente V3','nota',1000,2000,0,'PER_M2',0.0)"
        )
        db.execSQL(
            "INSERT INTO `wall_calculations` (`id`,`projectId`,`wallName`,`lengthMeters`,`heightMeters`," +
                "`faces`,`hasInsulation`,`studSpacingMeters`,`boardType`,`wasteFactor`,`totalDrywallAreaM2`," +
                "`totalBoardsCount`,`totalProfiles3mCount`,`summaryNotes`,`createdAt`,`netAreaM2`,`paintEnabled`," +
                "`paintFinishCoats`,`paintWasteFactor`,`paintProductName`,`paintUnit`," +
                "`paintCoverageM2PerUnit`,`paintAreaM2`,`paintSealerUnits`,`paintFinishUnits`) VALUES " +
                "(1,1,'Muro V3',4.0,2.6,'BOTH_FACES',1,0.407,'ESTANDAR_ST',0.08,20.8,7,12,'nota',1000,20.8,0,2,0.10,'','GALON',0.0,0.0,0,0)"
        )
        db.version = 3
        db.close()
    }

    @Test
    fun testMigration3To4() {
        createVersion3Database()

        val db = Room.databaseBuilder(context, DrywallDatabase::class.java, databaseName)
            .addMigrations(*DrywallDatabase.MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        val projectDao = db.projectDao()
        val projectWithCalcs = runBlocking { projectDao.getAllProjectsWithCalculations().first() }

        assertEquals(1, projectWithCalcs.size)
        assertEquals("Obra V3", projectWithCalcs.first().project.name)
        assertEquals(1, projectWithCalcs.first().calculations.size)
        assertEquals("MURO_DRYWALL", projectWithCalcs.first().calculations.first().elementType)

        db.close()
    }
}
