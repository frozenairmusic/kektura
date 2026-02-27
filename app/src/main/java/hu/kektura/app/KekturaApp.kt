package hu.kektura.app

import android.app.Application
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.repository.TrailRepository
import hu.kektura.app.data.seed.OktSegmentSeedData
import hu.kektura.app.data.seed.OktSegmentUrls
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
        // Ensure segments are seeded, then auto-download missing GPX files
        appScope.launch {
            seedSegmentsIfNeeded()
            downloadMissingGpxFiles()
        }
    }

    private suspend fun seedSegmentsIfNeeded() {
        database.gpxSegmentDao().insertAll(OktSegmentSeedData.segments)
    }

    /**
     * Downloads GPX for any OKT segments that don't yet have data.
     * Runs silently in background; UI observes LiveData and updates when data arrives.
     */
    private suspend fun downloadMissingGpxFiles() {
        for (seg in OktSegmentSeedData.segments) {
            val existing = database.gpxSegmentDao().getById(seg.id)
            if (existing?.hasData == true) continue          // already loaded

            val url = OktSegmentUrls.urls[seg.id] ?: continue
            val gpx = GpxDownloader.download(url) ?: continue

            repository.updateSegmentGpx(seg.id, gpx)
            repository.syncWaypointsFromGpxText(seg.id, gpx, seg.region)
        }
    }
}
