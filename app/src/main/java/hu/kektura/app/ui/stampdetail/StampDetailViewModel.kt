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
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
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
    val date: String,
    val notes: String = "",
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

            val flat = mutableListOf<StampListItem>()
            for (key in groupOrder) {
                val items = byGroup[key] ?: continue
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

    fun openCollectSheet(groupKey: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _collectSheet.value = CollectSheetState(groupKey = groupKey, date = today)
    }

    fun openCollectSheetForStamp(stampPointId: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ui = _stampUis.value.find { it.point.id == stampPointId } ?: return
        val key = groupKeyFor(ui.point.stampCode).ifBlank { ui.point.stampCode }
        _collectSheet.value = CollectSheetState(
            groupKey = key,
            date = today,
            targetStampId = stampPointId,
            targetStampName = ui.point.name
        )
    }

    fun dismissCollectSheet() { _collectSheet.value = null }

    fun updateCollectDate(date: String) { _collectSheet.update { it?.copy(date = date) } }

    fun updateCollectNote(note: String) { _collectSheet.update { it?.copy(notes = note) } }

    fun confirmCollect() = viewModelScope.launch {
        val sheet = _collectSheet.value ?: return@launch
        if (sheet.targetStampId != null) {
            repo.recordStamp(sheet.targetStampId, sheet.date, sheet.notes)
        } else {
            _stampUis.value
                .filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == sheet.groupKey }
                .forEach { repo.recordStamp(it.point.id, sheet.date, sheet.notes) }
        }
        _collectSheet.value = null
    }

    fun removeAllForGroup(groupKey: String) = viewModelScope.launch {
        _stampUis.value
            .filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == groupKey }
            .forEach { repo.removeAllStampsForPoint(it.point.id) }
    }

    fun removeAllForStamp(stampPointId: Int) = viewModelScope.launch {
        repo.removeAllStampsForPoint(stampPointId)
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
