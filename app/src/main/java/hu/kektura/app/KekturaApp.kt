package hu.kektura.app

import android.app.Application
import android.util.Log
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.remote.MetadataFetcher
import hu.kektura.app.data.repository.TrailRepository
import hu.kektura.app.data.seed.AkSegmentSeedData
import hu.kektura.app.data.seed.OktSegmentSeedData
import hu.kektura.app.data.seed.RpddkSegmentSeedData
import hu.kektura.app.util.GpxDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KekturaApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: TrailRepository by lazy { TrailRepository(database) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            seedSegmentsIfNeeded()
            syncFromMetadata()
        }
    }

    private suspend fun seedSegmentsIfNeeded() {
        val dao = database.gpxSegmentDao()
        dao.insertAll(OktSegmentSeedData.segments)
        dao.insertAll(AkSegmentSeedData.segments)
        dao.insertAll(RpddkSegmentSeedData.segments)
    }

    /**
     * Fetches metadata.json from GCS, compares dates with local data,
     * and downloads only GPX files that are newer than what we have.
     */
    private suspend fun syncFromMetadata() {
        val remoteMetas = MetadataFetcher.fetch() ?: return

        for (meta in remoteMetas) {
            val roomId = meta.roomId
            if (roomId < 0) continue

            val local = database.gpxSegmentDao().getById(roomId)
            if (local != null && local.hasData) {
                val localDate = normaliseToYmd(local.lastUpdated ?: "")
                if (localDate.isNotBlank() && !isRemoteNewer(localDate, meta.lastUpdated)) {
                    continue   // local is up-to-date
                }
            }

            val gpx = GpxDownloader.download(meta.gpxUrl) ?: continue
            repository.updateSegmentGpx(roomId, gpx, meta.lastUpdated)
            repository.syncWaypointsFromGpxText(roomId, gpx, local?.region ?: "")
            Log.d("KekturaApp", "Synced segment ${meta.trailKey}/${meta.segmentNumber}")
        }
    }

    /** Returns true when [remote] (yyyyMMdd) is strictly newer than [local] (yyyyMMdd). */
    private fun isRemoteNewer(local: String, remote: String): Boolean =
        remote > local

    /** Strips dashes from ISO-8601 dates so both formats compare as yyyyMMdd. */
    private fun normaliseToYmd(date: String): String = date.replace("-", "").take(8)
}

