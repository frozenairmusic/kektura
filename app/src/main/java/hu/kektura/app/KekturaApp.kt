package hu.kektura.app

import android.app.Application
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.repository.TrailRepository
import hu.kektura.app.data.seed.AkSegmentSeedData
import hu.kektura.app.data.seed.AkSegmentUrls
import hu.kektura.app.data.seed.OktSegmentSeedData
import hu.kektura.app.data.seed.OktSegmentUrls
import hu.kektura.app.data.seed.RpddkSegmentSeedData
import hu.kektura.app.data.seed.RpddkSegmentUrls
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
        val dao = database.gpxSegmentDao()
        dao.insertAll(OktSegmentSeedData.segments)
        dao.insertAll(AkSegmentSeedData.segments)
        dao.insertAll(RpddkSegmentSeedData.segments)
    }

    /**
     * Downloads GPX for any segments that don't yet have data, across all 3 trails.
     * Runs silently in background; UI observes LiveData and updates when data arrives.
     */
    private suspend fun downloadMissingGpxFiles() {
        val allSegmentsWithUrls: List<Pair<GpxSegment, Map<Int, String>>> = listOf(
            OktSegmentSeedData.segments   to OktSegmentUrls.urls,
            AkSegmentSeedData.segments    to AkSegmentUrls.urls,
            RpddkSegmentSeedData.segments to RpddkSegmentUrls.urls
        ).flatMap { (segs, urls) -> segs.map { it to urls } }

        for ((seg, urls) in allSegmentsWithUrls) {
            val existing = database.gpxSegmentDao().getById(seg.id)
            if (existing?.hasData == true) continue          // already loaded

            val url = urls[seg.id] ?: continue
            val gpx = GpxDownloader.download(url) ?: continue

            repository.updateSegmentGpx(seg.id, gpx)
            repository.syncWaypointsFromGpxText(seg.id, gpx, seg.region)
        }
    }
}

