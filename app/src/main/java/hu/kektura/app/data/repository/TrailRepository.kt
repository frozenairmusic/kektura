package hu.kektura.app.data.repository

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import hu.kektura.app.data.db.AppDatabase
import hu.kektura.app.data.db.HikeDate
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHike
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.util.GpxParser

enum class EntryUpdateResult { OK, CONFLICT, NOT_FOUND }

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
    suspend fun recordStamp(stampPointId: Int, date: String, notes: String = ""): Boolean {
        val hikeId = db.userHikeDao().getHikeByDate(date)?.id
            ?: db.userHikeDao().insertHike(UserHike(date = date)).toInt()
        var added = false
        // A stamp code shared by several segments (end of one / start of the next)
        // is one physical stamp: credit every point carrying the same code.
        for (id in idsSharingCode(stampPointId)) {
            val existing = db.userHikeDao().getStampInHike(hikeId, id)
            if (existing != null) {
                if (notes.isNotBlank() && existing.notes != notes) {
                    db.userHikeDao().updateStampNotes(existing.id, notes)
                }
                continue
            }
            val maxOrder = db.userHikeDao().getMaxOrderForHike(hikeId) ?: 0
            db.userHikeDao().insertHikeStamp(
                UserHikeStamp(
                    hikeId = hikeId,
                    stampPointId = id,
                    order = maxOrder + 1,
                    collectedAt = date,
                    notes = notes
                )
            )
            added = true
        }
        return added
    }

    /**
     * Resolves entry ids to the full set of entries they stand for: the entries themselves
     * plus the same-hike entries of every stamp point sharing their stamp code.
     */
    private suspend fun expandEntries(entryIds: List<Int>): List<UserHikeStamp> {
        val dao = db.userHikeDao()
        val result = LinkedHashMap<Int, UserHikeStamp>()
        for (eid in entryIds) {
            val e = dao.getStampEntry(eid) ?: continue
            result[e.id] = e
            for (pid in idsSharingCode(e.stampPointId)) {
                dao.getStampInHike(e.hikeId, pid)?.let { result[it.id] = it }
            }
        }
        return result.values.toList()
    }

    /**
     * Changes the date and/or note of existing collection entries. Entries are moved to the
     * hike of [newDate]. Refused (CONFLICT) if any of them is already recorded on [newDate].
     */
    suspend fun updateEntries(entryIds: List<Int>, newDate: String, notes: String): EntryUpdateResult =
        db.withTransaction {
            val dao = db.userHikeDao()
            val entries = expandEntries(entryIds)
            if (entries.isEmpty()) return@withTransaction EntryUpdateResult.NOT_FOUND
            val moving = entries.filter { it.collectedAt != newDate }
            if (moving.isNotEmpty()) {
                val target = dao.getHikeByDate(newDate)
                if (target != null &&
                    moving.any { dao.getStampInHike(target.id, it.stampPointId) != null }
                ) return@withTransaction EntryUpdateResult.CONFLICT
                val targetId = target?.id ?: dao.insertHike(UserHike(date = newDate)).toInt()
                var order = dao.getMaxOrderForHike(targetId) ?: 0
                for (m in moving) dao.moveStamp(m.id, targetId, ++order, newDate)
            }
            for (e in entries) dao.updateStampNotes(e.id, notes)
            dao.deleteEmptyHikes()
            EntryUpdateResult.OK
        }

    /** Deletes single collection entries (and their same-code copies); prunes empty hikes. */
    suspend fun deleteEntries(entryIds: List<Int>) {
        db.withTransaction {
            expandEntries(entryIds).forEach { db.userHikeDao().deleteStampEntry(it.id) }
            db.userHikeDao().deleteEmptyHikes()
        }
    }

    /** [stampPointId] plus all other stamp points with the identical stamp code. */
    private suspend fun idsSharingCode(stampPointId: Int): Set<Int> {
        val code = db.stampPointDao().getStampCode(stampPointId).orEmpty()
        val ids = linkedSetOf(stampPointId)
        if (code.isNotBlank()) ids.addAll(db.stampPointDao().getIdsByStampCode(code))
        return ids
    }

    /** hikeId -> date, for same-date segment-part computation. */
    val hikeDates: LiveData<List<HikeDate>> = db.userHikeDao().getHikeDatesLive()

    /** Removes ALL collection records for [stampPointId], then prunes now-empty hikes. */
    suspend fun removeAllStampsForPoint(stampPointId: Int) {
        for (id in idsSharingCode(stampPointId)) db.userHikeDao().deleteAllStampsForPoint(id)
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

