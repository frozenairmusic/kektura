package hu.kektura.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHike
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.data.model.UserStamp

@Database(
    entities = [GpxSegment::class, StampPoint::class, UserStamp::class, UserHike::class, UserHikeStamp::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpxSegmentDao(): GpxSegmentDao
    abstract fun stampPointDao(): StampPointDao
    abstract fun userStampDao(): UserStampDao
    abstract fun userHikeDao(): UserHikeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Adds the trailType column (defaults to 'OKT') and seeds new trail segments. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN trailType TEXT NOT NULL DEFAULT 'OKT'")
            }
        }

        /** Renames trailType values: DEL_DUNANTULI → RPDDK, ALFOLDI → AK. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE gpx_segments SET trailType = 'RPDDK' WHERE trailType = 'DEL_DUNANTULI'")
                db.execSQL("UPDATE gpx_segments SET trailType = 'AK' WHERE trailType = 'ALFOLDI'")
            }
        }

        /** Adds elevationGainM and elevationLossM columns to gpx_segments. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN elevationGainM INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN elevationLossM INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Adds code, durationMin, and stampCount columns to gpx_segments. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN code TEXT")
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN durationMin INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE gpx_segments ADD COLUMN stampCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Introduces the hike-based stamp model: user_hikes + user_hike_stamps.
         * Existing user_stamps rows are migrated into a single legacy finished hike.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_hikes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'finished'
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_hike_stamps (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        hikeId INTEGER NOT NULL,
                        stampPointId INTEGER NOT NULL,
                        `order` INTEGER NOT NULL,
                        collectedAt TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY (hikeId) REFERENCES user_hikes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_user_hike_stamps_hikeId ON user_hike_stamps (hikeId)"
                )
                // Migrate existing collected stamps into a legacy finished hike.
                db.execSQL(
                    "INSERT INTO user_hikes (date, notes, status) VALUES (date('now'), 'Korábbi bélyegzések', 'finished')"
                )
                db.execSQL("""
                    INSERT INTO user_hike_stamps (hikeId, stampPointId, `order`, collectedAt)
                    SELECT (SELECT id FROM user_hikes ORDER BY id DESC LIMIT 1),
                           stampPointId,
                           CAST(uid AS INTEGER),
                           ''
                    FROM (SELECT stampPointId, MIN(uid) AS uid FROM user_stamps
                          GROUP BY stampPointId ORDER BY uid)
                """.trimIndent())
            }
        }

        /** Adds the notes column to user_hike_stamps. */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_hike_stamps ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * Adds visible and lastUpdated columns to gpx_segments.
         * These were present in the entity definition but were never added via migration,
         * causing schema validation failures on devices that had an older install.
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use try/catch so the migration doesn't fail if a column already exists
                // (e.g. on devices where the column was somehow present from a fresh install).
                try {
                    db.execSQL("ALTER TABLE gpx_segments ADD COLUMN visible INTEGER NOT NULL DEFAULT 1")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE gpx_segments ADD COLUMN lastUpdated TEXT")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kektura.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

