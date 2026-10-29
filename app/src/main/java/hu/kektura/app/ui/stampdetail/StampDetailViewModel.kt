package hu.kektura.app.ui.stampdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.repository.EntryUpdateResult
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import hu.kektura.app.ui.common.StampHistoryRow
import hu.kektura.app.util.SegmentProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StampPointUi(val point: StampPoint, val collected: Boolean)

data class CollectSheetState(
    val groupKey: String,
    /** Non-null when the sheet is opened for a single stamp rather than the whole group. */
    val targetStampId: Int? = null,
    val targetStampName: String = ""
)

data class StampGroupUi(
    val groupKey: String,
    val groupName: String,
    val items: List<StampPointUi>,
    val expanded: Boolean = true
) {
    val allCollected: Boolean get() = items.isNotEmpty() && items.all { it.collected }
    /** Header state: stamped if ANY point of the group (_1, _2, ...) was stamped. */
    val anyCollected: Boolean get() = items.any { it.collected }
}

sealed class StampListItem {
    data class GroupHeader(val group: StampGroupUi) : StampListItem()
    data class StampEntry(val item: StampPointUi, val groupKey: String) : StampListItem()
}

class StampDetailViewModel(
    application: Application,
    private val segmentId: Int
) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    private val _segment = MutableStateFlow<GpxSegment?>(null)
    val segment: StateFlow<GpxSegment?> = _segment

    init {
        viewModelScope.launch { _segment.value = repo.getSegmentById(segmentId) }
    }

    /** Same-date segment parts and completion of this segment. */
    val progress: StateFlow<SegmentProgress.Result?> =
        combine(
            repo.getStampsBySegmentLive(segmentId).asFlow(),
            repo.allHikeStamps.asFlow(),
            repo.hikeDates.asFlow()
        ) { points, hikeStamps, hikeDates ->
            val dates = hikeDates.associate { it.id to it.date }
            SegmentProgress.compute(points, hikeStamps) { dates[it].orEmpty() } as SegmentProgress.Result?
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Tracks which groups have been explicitly collapsed; all others default to expanded.
    private val _collapsedGroups = MutableStateFlow<Set<String>>(emptySet())

    private val _stampUis: StateFlow<List<StampPointUi>> =
        combine(
            repo.getStampsBySegmentLive(segmentId).asFlow(),
            repo.collectedPointIds.asFlow()
        ) { points, ids ->
            val idSet = ids.toSet()
            points.map { StampPointUi(it, idSet.contains(it.id)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedList: StateFlow<List<StampListItem>> =
        combine(_stampUis, _collapsedGroups) { stampUis, collapsed ->
            val groupOrder = mutableListOf<String>()
            val byGroup = LinkedHashMap<String, MutableList<StampPointUi>>()

            for (ui in stampUis) {
                val key = groupKeyFor(ui.point.stampCode).ifBlank { ui.point.stampCode }
                if (!byGroup.containsKey(key)) {
                    groupOrder.add(key)
                    byGroup[key] = mutableListOf()
                }
                byGroup[key]!!.add(ui)
            }

            // Ascending by stamp number (OKTPH_07 < OKTPH_07_B < OKTPH_08 < OKTPH_100),
            // and ascending inside a group (_1 before _2). Blank codes go last.
            groupOrder.sortWith(NATURAL_KEY)

            val flat = mutableListOf<StampListItem>()
            for (key in groupOrder) {
                val items = byGroup[key]?.sortedWith(
                    compareBy<StampPointUi, String>(NATURAL_KEY) { it.point.stampCode }
                        .thenBy { it.point.id }
                ) ?: continue
                val isExpanded = !collapsed.contains(key)
                val groupName = items.map { it.point.name }.distinct().joinToString(" / ")
                flat.add(StampListItem.GroupHeader(StampGroupUi(key, groupName, items, isExpanded)))
                if (isExpanded) {
                    items.forEach { flat.add(StampListItem.StampEntry(it, key)) }
                }
            }
            flat as List<StampListItem>
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleGroup(key: String) {
        val current = _collapsedGroups.value
        _collapsedGroups.value = if (current.contains(key)) current - key else current + key
    }

    // ── Collect sheet ─────────────────────────────────────────────────────────

    private val _collectSheet = MutableStateFlow<CollectSheetState?>(null)
    val collectSheet: StateFlow<CollectSheetState?> = _collectSheet

    private val _sheetError = MutableStateFlow<String?>(null)
    val sheetError: StateFlow<String?> = _sheetError

    private fun keyOf(p: StampPoint) = groupKeyFor(p.stampCode).ifBlank { p.stampCode }

    fun openCollectSheet(groupKey: String) {
        _sheetError.value = null
        _collectSheet.value = CollectSheetState(groupKey = groupKey)
    }

    fun openCollectSheetForStamp(stampPointId: Int) {
        val ui = _stampUis.value.find { it.point.id == stampPointId } ?: return
        _sheetError.value = null
        _collectSheet.value = CollectSheetState(
            groupKey = keyOf(ui.point),
            targetStampId = stampPointId,
            targetStampName = ui.point.name
        )
    }

    fun dismissCollectSheet() {
        _collectSheet.value = null
        _sheetError.value = null
    }

    /** Dates on which the sheet's stamp (or any member of its group) was collected. */
    val sheetRows: StateFlow<List<StampHistoryRow>> =
        combine(_collectSheet, repo.allHikeStamps.asFlow(), _stampUis) { sheet, hikeStamps, uis ->
            if (sheet == null) return@combine emptyList<StampHistoryRow>()
            val pts = uis.map { it.point }.filter {
                if (sheet.targetStampId != null) it.id == sheet.targetStampId
                else keyOf(it) == sheet.groupKey
            }
            val byId = pts.associateBy { it.id }
            hikeStamps.filter { it.stampPointId in byId }
                .groupBy { it.collectedAt }
                .map { (date, entries) ->
                    val label = if (sheet.targetStampId != null || byId.size < 2) ""
                    else entries.mapNotNull { byId[it.stampPointId] }
                        .sortedBy { it.id }
                        .joinToString(", ") { p ->
                            p.stampCode.removePrefix(sheet.groupKey).ifBlank { p.name }
                        }
                    StampHistoryRow(
                        date = date,
                        notes = entries.map { it.notes }.filter { it.isNotBlank() }
                            .distinct().joinToString(" · "),
                        entryIds = entries.map { it.id },
                        label = label
                    )
                }
                .sortedByDescending { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(date: String, notes: String) = viewModelScope.launch {
        val sheet = _collectSheet.value ?: return@launch
        val ids = if (sheet.targetStampId != null) listOf(sheet.targetStampId)
        else _stampUis.value.filter { keyOf(it.point) == sheet.groupKey }.map { it.point.id }
        var added = false
        ids.forEach { if (repo.recordStamp(it, date, notes)) added = true }
        _sheetError.value = if (added) null else "Erre a napra már rögzítve van."
    }

    fun editEntry(row: StampHistoryRow, date: String, notes: String) = viewModelScope.launch {
        _sheetError.value = when (repo.updateEntries(row.entryIds, date, notes)) {
            EntryUpdateResult.CONFLICT -> "Erre a napra már rögzítve van."
            else -> null
        }
    }

    fun deleteEntry(row: StampHistoryRow) = viewModelScope.launch {
        repo.deleteEntries(row.entryIds)
        _sheetError.value = null
    }

    fun removeAllForGroup(groupKey: String) = viewModelScope.launch {
        _stampUis.value
            .filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == groupKey }
            .forEach { repo.removeAllStampsForPoint(it.point.id) }
    }

    fun removeAllForStamp(stampPointId: Int) = viewModelScope.launch {
        repo.removeAllStampsForPoint(stampPointId)
    }

    companion object {
        private val TOKEN = Regex("""\d+|\D+""")

        /** Natural order: digit runs compare numerically, other text alphabetically. */
        private val NATURAL_KEY = Comparator<String> { a, b ->
            if (a.isBlank() != b.isBlank()) return@Comparator if (a.isBlank()) 1 else -1
            val ta = TOKEN.findAll(a).map { it.value }.toList()
            val tb = TOKEN.findAll(b).map { it.value }.toList()
            for (i in 0 until minOf(ta.size, tb.size)) {
                val x = ta[i]
                val y = tb[i]
                val c = if (x[0].isDigit() && y[0].isDigit())
                    x.toBigInteger().compareTo(y.toBigInteger())
                else x.compareTo(y, ignoreCase = true)
                if (c != 0) return@Comparator c
            }
            ta.size - tb.size
        }
    }

    class Factory(
        private val application: Application,
        private val segmentId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StampDetailViewModel(application, segmentId) as T
    }
}
