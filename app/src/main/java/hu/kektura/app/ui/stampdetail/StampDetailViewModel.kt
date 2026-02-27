package hu.kektura.app.ui.stampdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
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

    private val _expandedGroups = MutableLiveData<Set<String>>(emptySet())

    private val _stampUis: MediatorLiveData<List<StampPointUi>> =
        MediatorLiveData<List<StampPointUi>>().apply {
            var points: List<StampPoint> = emptyList()
            var collectedIds: Set<Int> = emptySet()

            fun merge() {
                value = points.map { StampPointUi(it, collectedIds.contains(it.id)) }
            }

            addSource(repo.getStampsBySegmentLive(segmentId)) { pts -> points = pts; merge() }
            addSource(repo.collectedPointIds) { ids -> collectedIds = ids.toSet(); merge() }
        }

    val groupedList: MediatorLiveData<List<StampListItem>> =
        MediatorLiveData<List<StampListItem>>().apply {
            var stampUis: List<StampPointUi> = emptyList()
            var expanded: Set<String> = emptySet()

            fun rebuild() {
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
                    val isExpanded = expanded.contains(key)
                    val groupName = items.map { it.point.name }.distinct().joinToString(" / ")
                    flat.add(StampListItem.GroupHeader(StampGroupUi(key, groupName, items, isExpanded)))
                    if (isExpanded) {
                        items.forEach { flat.add(StampListItem.StampEntry(it, key)) }
                    }
                }
                value = flat
            }

            addSource(_stampUis)        { items -> stampUis = items; rebuild() }
            addSource(_expandedGroups)  { e     -> expanded = e;    rebuild() }
        }

    fun toggleGroup(key: String) {
        val current = _expandedGroups.value ?: emptySet()
        _expandedGroups.value = if (current.contains(key)) current - key else current + key
    }

    fun collectGroup(key: String) = viewModelScope.launch {
        _stampUis.value
            ?.filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == key && !it.collected }
            ?.forEach { repo.collectStamp(it.point.id) }
    }

    fun removeGroup(key: String) = viewModelScope.launch {
        _stampUis.value
            ?.filter { groupKeyFor(it.point.stampCode).ifBlank { it.point.stampCode } == key && it.collected }
            ?.forEach { repo.removeStamp(it.point.id) }
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
