package hu.kektura.app.data.repository

import androidx.lifecycle.LiveData
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserStamp
import hu.kektura.app.util.GpxParser
import java.time.Instant

class TrailRepository(private val db: AppDatabase) {

    // ── Segments ──────────────────────────────────────────────────────────────

    val allSegments: LiveData<List<GpxSegment>> =
        db.gpxSegmentDao().getAllLive()

    suspend fun getSegment(id: Int): GpxSegment? =
        db.gpxSegmentDao().getById(id)

    suspend fun updateSegmentGpx(id: Int, gpxContent: String) {
        db.gpxSegmentDao().updateGpxContent(id, gpxContent, Instant.now().toString())
    }

    // ── Stamp points ──────────────────────────────────────────────────────────

    val allStampPoints: LiveData<List<StampPoint>> =
        db.stampPointDao().getAllLive()

    fun getStampsBySegmentLive(segId: Int): LiveData<List<StampPoint>> =
        db.stampPointDao().getBySegmentIdLive(segId)

    /**
     * Parses waypoints from the provided GPX text and upserts stamp points
     * for [segmentId]. IDs are assigned as segmentId * 1000 + (1-based index).
     *
     * Stamp code is extracted from <desc> by finding the last (OKTPH_XX) group.
     */
    suspend fun syncWaypointsFromGpxText(
        segmentId: Int,
        gpxText: String,
        regionName: String = ""
    ) {
        val wpts = GpxParser.parseWaypoints(gpxText.byteInputStream())
        if (wpts.isEmpty()) return
        db.stampPointDao().deleteBySegmentId(segmentId)
        val points = wpts.mapIndexed { i, w ->
            StampPoint(
                id        = segmentId * 1000 + (i + 1),
                name      = w.name.ifBlank { "OKT-%02d #%d".format(segmentId, i + 1) },
                latitude  = w.lat,
                longitude = w.lon,
                region    = regionName,
                segmentId = segmentId,
                notes     = w.desc,
                elevation = w.ele,
                stampCode = parseStampCode(w.desc)
            )
        }
        db.stampPointDao().insertAll(points)
    }

    // ── User collected stamps ─────────────────────────────────────────────────

    val collectedPointIds: LiveData<List<Int>> =
        db.userStampDao().getCollectedIdSetLive()

    val collectedCount: LiveData<Int> =
        db.userStampDao().countLive()

    suspend fun collectStamp(stampPointId: Int) {
        db.userStampDao().insert(UserStamp(stampPointId = stampPointId))
    }

    suspend fun removeStamp(pointId: Int) {
        db.userStampDao().deleteByPointId(pointId)
    }

    companion object {

        /**
         * Extracts the last (OKTPH_XX) code from a GPX waypoint <desc>.
         * e.g. "Liptói-menedékház (EM143INF) (OKTPH_100_2)" → "OKTPH_100_2"
         */
        fun parseStampCode(desc: String): String {
            val regex = Regex("""\(([^)]+)\)""")
            val matches = regex.findAll(desc).map { it.groupValues[1] }.toList()
            return matches.lastOrNull { it.startsWith("OKTPH") } ?: ""
        }

        /**
         * Derives the display group key:
         *   OKTPH_07      → OKTPH_07
         *   OKTPH_07_1    → OKTPH_07   (numeric suffix → same physical location)
         *   OKTPH_07_2    → OKTPH_07
         *   OKTPH_07_B    → OKTPH_07_B (letter → inserted stamp, own group)
         *   OKTPH_07_B_1  → OKTPH_07_B
         */
        fun groupKeyFor(stampCode: String): String {
            if (stampCode.isBlank()) return ""
            val m = Regex("""^(OKTPH_\d+(?:_[A-Za-z]+)?)""").find(stampCode)
            return m?.groupValues?.get(1) ?: stampCode
        }
    }
}
