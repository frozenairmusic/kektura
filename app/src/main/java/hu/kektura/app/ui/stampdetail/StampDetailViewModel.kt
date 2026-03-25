package hu.kektura.app.ui.stampdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StampPointUi(val point: StampPoint, val collected: Boolean)

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

    fun collectGroup(key: String) = viewModelScope.launch {
        _stampUis.value
            .filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == key && !it.collected }
            .forEach { repo.collectStamp(it.point.id) }
    }

    fun removeGroup(key: String) = viewModelScope.launch {
        _stampUis.value
            .filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == key && it.collected }
            .forEach { repo.removeStamp(it.point.id) }
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
