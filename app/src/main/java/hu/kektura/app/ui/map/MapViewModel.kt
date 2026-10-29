package hu.kektura.app.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import hu.kektura.app.util.GpxParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TrailSection(val points: List<GpxParser.TrackPoint>, val completed: Boolean)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    /** Selected trail types driven from SettingsStore (e.g. ["OKT"]). */
    val selectedTrailTypes = MutableStateFlow(listOf("OKT"))

    /** Trail segments that have GPX data downloaded, filtered to the selected trail. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val segments: StateFlow<List<GpxSegment>> =
        selectedTrailTypes.flatMapLatest { types ->
            repo.getSegmentsByTrailTypesLive(types).asFlow()
                .map { segs -> segs.filter { it.hasData && it.visible } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Stamp points filtered to only those belonging to the currently visible segments. */
    val stampPoints: StateFlow<List<StampPoint>> =
        combine(
            segments,
            repo.allStampPoints.asFlow()
        ) { segs, allStamps ->
            val segIds = segs.map { it.id }.toSet()
            allStamps.filter { it.segmentId in segIds }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val collectedIds: StateFlow<Set<Int>> =
        repo.collectedPointIds
            .asFlow()
            .map { it.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** All hike stamp records — used both for completedLegs and for per-stamp history in the map sheet. */
    val allHikeStamps: StateFlow<List<UserHikeStamp>> =
        repo.allHikeStamps
            .asFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Set of (fromGroupKey, toGroupKey) pairs that appear consecutively in at least one hike.
     * Used to determine whether a trail section between two stamp groups is completed.
     */
    val completedLegs: StateFlow<Set<Pair<String, String>>> =
        combine(
            allHikeStamps,
            repo.allStampPoints.asFlow()
        ) { hikeStamps, allPoints ->
            withContext(Dispatchers.Default) {
                val groupKeyById = allPoints.associate { sp ->
                    sp.id to groupKeyFor(sp.stampCode).ifBlank { sp.stampCode }
                }
                hikeStamps.groupBy { it.hikeId }.values.flatMap { stamps ->
                    stamps.sortedBy { it.order }.zipWithNext { a, b ->
                        val fromKey = groupKeyById[a.stampPointId] ?: return@zipWithNext null
                        val toKey = groupKeyById[b.stampPointId] ?: return@zipWithNext null
                        if (fromKey == toKey) null else fromKey to toKey
                    }.filterNotNull()
                }.toSet()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /**
     * Trail split into sections. A section between two adjacent stamp groups is
     * marked [TrailSection.completed] when both group keys appear consecutively in any hike.
     */
    val trailSections: StateFlow<List<TrailSection>> =
        combine(segments, stampPoints, completedLegs) { segs, stamps, legs ->
            withContext(Dispatchers.Default) {
                segs.flatMap { seg -> computeSections(seg, stamps, legs) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Re-syncs stamp point locations from the segment's embedded GPX waypoints. */
    fun syncWaypointsFromGpx(segment: GpxSegment) {
        val gpxContent = segment.gpxContent ?: return
        viewModelScope.launch {
            repo.syncWaypointsFromGpxText(segment.id, gpxContent, segment.region)
        }
    }

    /** Records this stamp point under today's date. Multiple stamps on the same day share a hike. */
    fun quickRecordToday(stampPointId: Int) {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            repo.recordStamp(stampPointId, today)
        }
    }

    /** Removes all collection records for this stamp point (prunes empty hikes automatically). */
    fun quickRemoveAll(stampPointId: Int) {
        viewModelScope.launch { repo.removeAllStampsForPoint(stampPointId) }
    }

    companion object {

        private fun computeSections(
            seg: GpxSegment,
            stamps: List<StampPoint>,
            completedLegs: Set<Pair<String, String>>
        ): List<TrailSection> {
            val gpx = seg.gpxContent ?: return emptyList()
            val track = try {
                GpxParser.parseTracks(gpx.byteInputStream())
            } catch (_: Exception) {
                return emptyList()
            }
            // Only use actual stamp stations (those with a recognized stamp code) as
            // section boundaries. Informational waypoints (junctions, parking, etc.)
            // in the GPX must not split sections.
            val segStamps = stamps
                .filter { it.segmentId == seg.id && it.stampCode.isNotBlank() }
                .sortedBy { it.id }
            val sections = splitTrackByStamps(track, segStamps, completedLegs)
            Log.d("TrailSections", "seg ${seg.id}: ${track.size} pts, " +
                    "${segStamps.size} stamps, ${sections.count { it.completed }} completed sections")
            return sections
        }

        private fun splitTrackByStamps(
            track: List<GpxParser.TrackPoint>,
            stamps: List<StampPoint>,
            completedLegs: Set<Pair<String, String>>
        ): List<TrailSection> {
            if (track.isEmpty()) return emptyList()
            if (stamps.isEmpty()) return listOf(TrailSection(track, false))

            // Snap each stamp to its nearest track point index, then sort by that index.
            val snapped = stamps
                .map { sp -> sp to nearestTrackIndex(track, sp.latitude, sp.longitude) }
                .sortedBy { it.second }

            val sections = mutableListOf<TrailSection>()
            var cursor = 0

            snapped.forEachIndexed { i, (stamp, snapIdx) ->
                val end = snapIdx.coerceIn(cursor, track.lastIndex)
                if (end > cursor) {
                    val prevStamp = if (i == 0) null else snapped[i - 1].first
                    val completed = prevStamp != null && run {
                        val fromKey = groupKeyFor(prevStamp.stampCode).ifBlank { prevStamp.stampCode }
                        val toKey = groupKeyFor(stamp.stampCode).ifBlank { stamp.stampCode }
                        completedLegs.contains(fromKey to toKey)
                    }
                    sections.add(TrailSection(track.subList(cursor, end + 1), completed))
                    cursor = end
                }
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
}
