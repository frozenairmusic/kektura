package hu.kektura.app.data.repository

import androidx.lifecycle.LiveData
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHike
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.util.GpxParser

class TrailRepository(private val db: AppDatabase) {

    // ── Segments ──────────────────────────────────────────────────────────────

    fun getSegmentsByTrailTypesLive(trailTypes: List<String>): LiveData<List<GpxSegment>> =
        db.gpxSegmentDao().getByTrailTypesLive(trailTypes)

    suspend fun getSegmentById(id: Int): GpxSegment? = db.gpxSegmentDao().getById(id)

    suspend fun updateSegmentGpx(id: Int, gpxContent: String, lastUpdated: String) {
        db.gpxSegmentDao().updateGpxContent(id, gpxContent, lastUpdated)
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
     * Stamp code is extracted from <desc> by finding the last parenthesized code
     * that matches a known trail stamp prefix (OKTPH, DDKPH, AKPH).
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
                name      = w.name.ifBlank { "#%d".format(segmentId) },
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

    // ── User hike model ───────────────────────────────────────────────────────

    /** Distinct stamp point IDs from all hikes — drives dot colours on the map. */
    val collectedPointIds: LiveData<List<Int>> =
        db.userHikeDao().getCollectedStampIdsLive()

    /** All hike stamps ordered by hikeId/order — used for section-completion computation. */
    val allHikeStamps: LiveData<List<UserHikeStamp>> =
        db.userHikeDao().getAllHikeStampsLive()

    /**
     * Records [stampPointId] for [date] (ISO "yyyy-MM-dd").
     * All stamps on the same date are grouped into one hike entry automatically.
     * The same stamp can be recorded on multiple dates (shared endpoint support).
     * Duplicate within the same day's hike is silently ignored.
     */
    suspend fun recordStamp(stampPointId: Int, date: String, notes: String = "") {
        val hikeId = db.userHikeDao().getHikeByDate(date)?.id
            ?: db.userHikeDao().insertHike(UserHike(date = date)).toInt()
        if (db.userHikeDao().getStampInHike(hikeId, stampPointId) != null) return
        val maxOrder = db.userHikeDao().getMaxOrderForHike(hikeId) ?: 0
        db.userHikeDao().insertHikeStamp(
            UserHikeStamp(
                hikeId = hikeId,
                stampPointId = stampPointId,
                order = maxOrder + 1,
                collectedAt = date,
                notes = notes
            )
        )
    }

    /** Removes ALL collection records for [stampPointId], then prunes now-empty hikes. */
    suspend fun removeAllStampsForPoint(stampPointId: Int) {
        db.userHikeDao().deleteAllStampsForPoint(stampPointId)
        db.userHikeDao().deleteEmptyHikes()
    }

    companion object {

        private val STAMP_PREFIXES = listOf("OKTPH", "DDKPH", "AKPH")

        /**
         * Extracts the last stamp code from a GPX waypoint <desc>.
         * Handles all three trail types:
         *  - OKT  → OKTPH_XX
         *  - RPDDK → DDKPH_XX
         *  - AK   → AKPH_XX
         *
         * e.g. "Liptói-menedékház (EM143INF) (OKTPH_100_2)" → "OKTPH_100_2"
         */
        fun parseStampCode(desc: String): String {
            val regex = Regex("""\(([^)]+)\)""")
            val matches = regex.findAll(desc).map { it.groupValues[1] }.toList()
            return matches.lastOrNull { code ->
                STAMP_PREFIXES.any { prefix -> code.startsWith(prefix) }
            } ?: ""
        }

        /**
         * Derives the display group key by stripping numeric-only suffixes:
         *   OKTPH_07      → OKTPH_07
         *   OKTPH_07_1    → OKTPH_07   (numeric suffix → same physical location)
         *   OKTPH_07_2    → OKTPH_07
         *   OKTPH_07_B    → OKTPH_07_B (letter → inserted stamp, own group)
         *   DDKPH_03_1    → DDKPH_03
         *   AKPH_05       → AKPH_05
         */
        fun groupKeyFor(stampCode: String): String {
            if (stampCode.isBlank()) return ""
            val m = Regex("""^([A-Z]+PH_\d+(?:_[A-Za-z]+)?)""").find(stampCode)
            return m?.groupValues?.get(1) ?: stampCode
        }
    }
}

