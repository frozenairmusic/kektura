package hu.kektura.app.ui.stamps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope

data class SegmentRow(
    val segment: GpxSegment,
    val stampCount: Int,
    val collectedCount: Int
)

class StampsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    /** Selected trail types (as TrailType.name strings), e.g. ["OKT", "AK"] */
    val selectedTrailTypes = MutableStateFlow(listOf("OKT"))

    @Suppress("UNCHECKED_CAST")
    val segmentRows: StateFlow<List<SegmentRow>> =
        selectedTrailTypes
            .flatMapLatest { types ->
                val segFlow = repo.getSegmentsByTrailTypesLive(types).asFlow()
                val stampFlow = repo.allStampPoints.asFlow()
                val collectedFlow = repo.collectedPointIds.asFlow()
                combine(segFlow, stampFlow, collectedFlow) { segs, stamps, ids ->
                    val bySegment = stamps.groupBy { it.segmentId }
                    val idSet = ids.toSet()
                    segs.map { seg ->
                        val segStamps = bySegment[seg.id] ?: emptyList()
                        val groups = segStamps.groupBy { sp ->
                            groupKeyFor(sp.stampCode).ifBlank { sp.stampCode }
                        }
                        val collected = groups.values.count { grp ->
                            grp.isNotEmpty() && grp.all { idSet.contains(it.id) }
                        }
                        SegmentRow(seg, groups.size, collected)
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

