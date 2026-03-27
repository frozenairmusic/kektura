package hu.kektura.app.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    /** Trail segments that have GPX data downloaded and are set visible. */
    val segments: StateFlow<List<GpxSegment>> =
        repo.getSegmentsByTrailTypesLive(listOf("OKT", "RPDDK", "AK"))
            .asFlow()
            .map { segs -> segs.filter { it.hasData && it.visible } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stampPoints: StateFlow<List<StampPoint>> =
        repo.allStampPoints
            .asFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val collectedIds: StateFlow<Set<Int>> =
        repo.collectedPointIds
            .asFlow()
            .map { it.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Re-syncs stamp point locations from the segment's embedded GPX waypoints. */
    fun syncWaypointsFromGpx(segment: GpxSegment) {
        val gpxContent = segment.gpxContent ?: return
        viewModelScope.launch {
            repo.syncWaypointsFromGpxText(segment.id, gpxContent, segment.region)
        }
    }

    fun markCollected(stampPointId: Int) {
        viewModelScope.launch { repo.collectStamp(stampPointId) }
    }

    fun removeStamp(stampPointId: Int) {
        viewModelScope.launch { repo.removeStamp(stampPointId) }
    }
}
