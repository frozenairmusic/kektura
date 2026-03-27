package hu.kektura.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserStamp

@Database(
    entities = [GpxSegment::class, StampPoint::class, UserStamp::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpxSegmentDao(): GpxSegmentDao
    abstract fun stampPointDao(): StampPointDao
    abstract fun userStampDao(): UserStampDao

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

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kektura.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

