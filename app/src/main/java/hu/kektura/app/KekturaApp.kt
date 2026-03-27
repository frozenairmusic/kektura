package hu.kektura.app

import android.app.Application
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.remote.MetadataFetcher
import hu.kektura.app.data.repository.TrailRepository
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
        appScope.launch { syncFromMetadata() }
    }

    /**
     * Fetches metadata.json from GCS, upserts segment rows (insert on first run,
     * update name/distance if changed), then downloads GPX only for segments whose
     * remote last_updated differs from the locally stored value.
     */
    private suspend fun syncFromMetadata() {
        val remoteSegments = MetadataFetcher.fetch() ?: return
        val dao = database.gpxSegmentDao()

        // 1. Insert any segment that doesn't exist yet (IGNORE keeps existing rows intact)
        dao.insertAll(remoteSegments.filter { it.roomId > 0 }.map { it.toGpxSegment() })

        // 2. Update name/distance from metadata for all known segments
        for (meta in remoteSegments) {
            if (meta.roomId <= 0) continue
            dao.updateMetadata(meta.roomId, meta.title, meta.distanceKm, meta.elevationGainM, meta.elevationLossM)
        }

        // 3. Download GPX only for segments that are new or have an updated last_updated
        for (meta in remoteSegments) {
            val local = dao.getById(meta.roomId) ?: continue
            if (local.hasData && local.lastUpdated == meta.lastUpdated) continue

            val gpx = GpxDownloader.download(meta.gpxUrl) ?: continue
            repository.updateSegmentGpx(local.id, gpx, meta.lastUpdated)
            repository.syncWaypointsFromGpxText(local.id, gpx, local.region)
        }
    }
}


