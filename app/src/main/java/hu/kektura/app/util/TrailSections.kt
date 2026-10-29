package hu.kektura.app.util

import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TrailSection(val points: List<GpxParser.TrackPoint>, val completed: Boolean)

/**
 * Splits a segment's track at its stamp locations and decides which stretches are collected.
 * Shared by the map (drawing) and the stats screen (distance / elevation).
 */
object TrailSections {

    /** stamp code -> dates on which any stamp point with that code was stamped. */
    fun datesByCode(
        hikeStamps: List<UserHikeStamp>,
        allPoints: List<StampPoint>
    ): Map<String, Set<String>> {
        val codeById = allPoints.associate { it.id to it.stampCode }
        val result = HashMap<String, MutableSet<String>>()
        for (hs in hikeStamps) {
            val code = codeById[hs.stampPointId].orEmpty()
            if (code.isNotBlank()) result.getOrPut(code) { mutableSetOf() }.add(hs.collectedAt)
        }
        return result
    }

    private val trackCache = HashMap<Int, Pair<Int, List<GpxParser.TrackPoint>>>()

    private fun trackOf(seg: GpxSegment): List<GpxParser.TrackPoint>? {
        val gpx = seg.gpxContent ?: return null
        val sig = gpx.hashCode() * 31 + gpx.length
        synchronized(trackCache) {
            trackCache[seg.id]?.let { if (it.first == sig) return it.second }
        }
        val track = try {
            GpxParser.parseTracks(gpx.byteInputStream())
        } catch (_: Exception) {
            return null
        }
        synchronized(trackCache) { trackCache[seg.id] = sig to track }
        return track
    }

    /**
     * Sections of [seg]'s track, in trail order. Returns an empty list when the segment has no
     * downloaded track. [stamps] may contain points of other segments; they are filtered out.
     */
    fun build(
        seg: GpxSegment,
        stamps: List<StampPoint>,
        datesByCode: Map<String, Set<String>>,
        onLog: ((String) -> Unit)? = null
    ): List<TrailSection> {
        val track = trackOf(seg) ?: return emptyList()
        // Only real stamp stations (with a recognised stamp code) split sections;
        // informational waypoints must not.
        val segStamps = stamps
            .filter { it.segmentId == seg.id && it.stampCode.isNotBlank() }
            .sortedBy { it.id }
        return splitTrackByStamps(track, segStamps, datesByCode, onLog)
    }

    // ── Measuring ────────────────────────────────────────────────────────────

    fun lengthKm(points: List<GpxParser.TrackPoint>): Double {
        var sum = 0.0
        for (i in 1 until points.size) sum += haversineKm(points[i - 1], points[i])
        return sum
    }

    /** Sum of positive / negative elevation steps (metres). Zero when the track has no <ele>. */
    fun gainLossM(points: List<GpxParser.TrackPoint>): Pair<Double, Double> {
        var gain = 0.0
        var loss = 0.0
        for (i in 1 until points.size) {
            val d = points[i].ele - points[i - 1].ele
            if (d > 0) gain += d else loss += -d
        }
        return gain to loss
    }

    private fun haversineKm(a: GpxParser.TrackPoint, b: GpxParser.TrackPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(h), sqrt(1 - h))
    }

    // ── Splitting ────────────────────────────────────────────────────────────

    /** One physical stamp location (all its _1/_2/... places) snapped onto the track. */
    private class GroupSpan(
        val key: String,
        val first: Int,
        val last: Int,
        /** Dates on which any place of this location was stamped. */
        val dates: Set<String>
    ) {
        val stamped: Boolean get() = dates.isNotEmpty()
    }

    /**
     * Splits the track at stamp LOCATIONS (groups), ordered by position along the track:
     *  - the stretch between two adjacent locations is completed when both were stamped
     *    on a common date (any place of a multi-place location counts for the location);
     *  - the stretch between the first and last place of one location (e.g. _1 -> _2)
     *    is completed as soon as ANY place of that location is stamped.
     * The stretch before the first and after the last location is never completed.
     */
    private fun splitTrackByStamps(
        track: List<GpxParser.TrackPoint>,
        stamps: List<StampPoint>,
        datesByCode: Map<String, Set<String>>,
        onLog: ((String) -> Unit)?
    ): List<TrailSection> {
        if (track.isEmpty()) return emptyList()
        if (stamps.isEmpty()) return listOf(TrailSection(track, false))

        val spans = stamps
            .groupBy { groupKeyFor(it.stampCode).ifBlank { it.stampCode } }
            .map { (key, pts) ->
                val idx = pts.map { nearestTrackIndex(track, it.latitude, it.longitude) }
                GroupSpan(
                    key = key,
                    first = idx.minOrNull() ?: 0,
                    last = idx.maxOrNull() ?: 0,
                    dates = pts.flatMap { datesByCode[it.stampCode].orEmpty() }.toSet()
                )
            }
            .sortedBy { it.first }

        onLog?.invoke(spans.joinToString(" | ") {
            "${it.key}[${it.first}..${it.last}] ${it.dates.sorted()}"
        })

        val sections = mutableListOf<TrailSection>()
        var cursor = 0
        var prev: GroupSpan? = null

        for (span in spans) {
            // Leg from the previous location to this one.
            val start = span.first.coerceIn(cursor, track.lastIndex)
            if (start > cursor) {
                val p = prev
                val done = p != null && p.dates.intersect(span.dates).isNotEmpty()
                sections.add(TrailSection(track.subList(cursor, start + 1), done))
                cursor = start
            }
            // Stretch between this location's own places (_1 .. _n).
            val end = span.last.coerceIn(cursor, track.lastIndex)
            if (end > cursor) {
                sections.add(TrailSection(track.subList(cursor, end + 1), span.stamped))
                cursor = end
            }
            prev = span
        }

        // Tail section after the last stamp — never completed.
        if (cursor < track.lastIndex) {
            sections.add(TrailSection(track.subList(cursor, track.size), false))
        }
        return sections
    }

    private fun nearestTrackIndex(
        track: List<GpxParser.TrackPoint>,
        lat: Double,
        lon: Double
    ): Int {
        var best = 0
        var bestDist = Double.MAX_VALUE
        track.forEachIndexed { i, pt ->
            val d = (pt.lat - lat) * (pt.lat - lat) + (pt.lon - lon) * (pt.lon - lon)
            if (d < bestDist) { bestDist = d; best = i }
        }
        return best
    }
}
