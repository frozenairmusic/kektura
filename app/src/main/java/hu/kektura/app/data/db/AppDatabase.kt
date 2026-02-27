package hu.kektura.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserStamp
import hu.kektura.app.data.seed.OktSegmentSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [GpxSegment::class, StampPoint::class, UserStamp::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpxSegmentDao(): GpxSegmentDao
    abstract fun stampPointDao(): StampPointDao
    abstract fun userStampDao(): UserStampDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kektura.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed the 27 OKT segments on first create
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.gpxSegmentDao()?.insertAll(OktSegmentSeedData.segments)
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
