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
import hu.kektura.app.data.repository.EntryUpdateResult
import hu.kektura.app.util.TrailSection
import hu.kektura.app.util.TrailSections
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

    /** All hike stamp records — used for per-stamp history in the map sheet. */
    val allHikeStamps: StateFlow<List<UserHikeStamp>> =
        repo.allHikeStamps
            .asFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * stamp code -> dates on which any stamp point with that code was stamped.
     * Points sharing a code are one physical stamp (e.g. shared segment endpoints), so
     * this is deliberately independent of which segment's row was tapped.
     */
    val datesByCode: StateFlow<Map<String, Set<String>>> =
        combine(allHikeStamps, repo.allStampPoints.asFlow()) { hikeStamps, allPoints ->
            withContext(Dispatchers.Default) { TrailSections.datesByCode(hikeStamps, allPoints) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * Trail split into sections, in TRAIL order (stamp locations snapped onto the track).
     * A section between two adjacent stamp locations is [TrailSection.completed] when both
     * were stamped on a common date; a location with several stamp places (_1, _2, ...)
     * counts as stamped when any of them is.
     */
    val trailSections: StateFlow<List<TrailSection>> =
        combine(segments, stampPoints, datesByCode) { segs, stamps, dates ->
            withContext(Dispatchers.Default) {
                segs.flatMap { seg ->
                    TrailSections.build(seg, stamps, dates) { Log.d("TrailSections", "seg ${seg.id}: $it") }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Re-syncs stamp point locations from the segment's embedded GPX waypoints. */
    fun syncWaypointsFromGpx(segment: GpxSegment) {
        val gpxContent = segment.gpxContent ?: return
        viewModelScope.launch {
            repo.syncWaypointsFromGpxText(segment.id, gpxContent, segment.region)
        }
    }

    private val _sheetError = MutableStateFlow<String?>(null)
    val sheetError: StateFlow<String?> = _sheetError

    fun clearSheetError() { _sheetError.value = null }

    /** Records [stampPointId] for any (past) date with an optional note. */
    fun addEntry(stampPointId: Int, date: String, notes: String) {
        viewModelScope.launch {
            val added = repo.recordStamp(stampPointId, date, notes)
            _sheetError.value = if (added) null else "Erre a napra már rögzítve van."
        }
    }

    fun editEntries(entryIds: List<Int>, date: String, notes: String) {
        viewModelScope.launch {
            _sheetError.value = when (repo.updateEntries(entryIds, date, notes)) {
                EntryUpdateResult.CONFLICT -> "Erre a napra már rögzítve van."
                else -> null
            }
        }
    }

    fun deleteEntries(entryIds: List<Int>) {
        viewModelScope.launch { repo.deleteEntries(entryIds); _sheetError.value = null }
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
}
