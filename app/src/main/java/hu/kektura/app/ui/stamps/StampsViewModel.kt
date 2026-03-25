package hu.kektura.app.ui.stamps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SegmentRow(
    val segment: GpxSegment,
    val stampCount: Int,
    val collectedCount: Int
)

class StampsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    /** Selected trail types (as TrailType.name strings), e.g. ["OKT", "AK"] */
    val selectedTrailTypes = MutableStateFlow(listOf("OKT"))

    /**
     * All segments for the currently selected trails, enriched with live stamp/collected counts.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentRows: StateFlow<List<SegmentRow>> =
        combine(
            selectedTrailTypes.flatMapLatest { types ->
                repo.getSegmentsByTrailTypesLive(types).asFlow()
            },
            repo.allStampPoints.asFlow(),
            repo.collectedPointIds.asFlow()
        ) { segments, allStamps, collectedIdsList ->
            val collectedIds = collectedIdsList.toSet()
            val bySegment = allStamps.groupBy { it.segmentId }
            segments.map { seg ->
                val stamps = bySegment[seg.id] ?: emptyList()
                val groups = stamps.groupBy { sp ->
                    groupKeyFor(sp.stampCode).ifBlank { sp.stampCode }
                }
                val collected = groups.values.count { grp ->
                    grp.isNotEmpty() && grp.all { collectedIds.contains(it.id) }
                }
                SegmentRow(seg, groups.size, collected)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}


