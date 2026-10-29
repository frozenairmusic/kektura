package hu.kektura.app

import android.app.Application
import android.content.SharedPreferences
import hu.kektura.app.data.db.AppDatabase
import org.maplibre.android.MapLibre
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
    private val prefs: SharedPreferences by lazy { getSharedPreferences("kektura_prefs", MODE_PRIVATE) }

    companion object {
        private const val PREF_LAST_METADATA_FETCH = "last_metadata_fetch"
        private const val METADATA_CACHE_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        appScope.launch { syncFromMetadata() }
    }

    /**
     * Fetches metadata.json from GCS, upserts segment rows (insert on first run,
     * update name/distance/etc. if changed), then downloads GPX only for segments
     * whose remote last_updated differs from the locally stored value.
     *
     * The 7-day staleness check is commented out for local testing — uncomment to
     * skip re-fetching when the cached metadata is still fresh.
     */
    private suspend fun syncFromMetadata() {
        // Skip if metadata was fetched less than 7 days ago (comment out for local testing)
        // val lastFetch = prefs.getLong(PREF_LAST_METADATA_FETCH, 0L)
        // if (System.currentTimeMillis() - lastFetch < METADATA_CACHE_MS) return

        val remoteSegments = MetadataFetcher.fetch() ?: return
        prefs.edit().putLong(PREF_LAST_METADATA_FETCH, System.currentTimeMillis()).apply()

        val dao = database.gpxSegmentDao()

        // 1. Insert any segment that doesn't exist yet (IGNORE keeps existing rows intact)
        dao.insertAll(remoteSegments.filter { it.roomId > 0 }.map { it.toGpxSegment() })

        // 2. Update name/distance/elevation/code/duration/stampCount from metadata
        for (meta in remoteSegments) {
            if (meta.roomId <= 0) continue
            dao.updateMetadata(
                id          = meta.roomId,
                name        = meta.title,
                distanceKm  = meta.distanceKm,
                gainM       = meta.elevationGainM,
                lossM       = meta.elevationLossM,
                code        = meta.code,
                durationMin = meta.durationMin,
                stampCount  = meta.stampCount
            )
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


