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
import hu.kektura.app.data.seed.AkSegmentSeedData
import hu.kektura.app.data.seed.OktSegmentSeedData
import hu.kektura.app.data.seed.RpddkSegmentSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [GpxSegment::class, StampPoint::class, UserStamp::class],
    version = 2,
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

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kektura.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed all trail segments on first database creation
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = INSTANCE?.gpxSegmentDao() ?: return@launch
                                dao.insertAll(OktSegmentSeedData.segments)
                                dao.insertAll(AkSegmentSeedData.segments)
                                dao.insertAll(RpddkSegmentSeedData.segments)
                            }
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Seed new trail segments if they were added after the initial install
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = INSTANCE?.gpxSegmentDao() ?: return@launch
                                dao.insertAll(AkSegmentSeedData.segments)
                                dao.insertAll(RpddkSegmentSeedData.segments)
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

