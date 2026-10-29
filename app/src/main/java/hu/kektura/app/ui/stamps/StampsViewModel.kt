package hu.kektura.app.ui.stamps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.util.SegmentProgress
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
    val collectedCount: Int,
    val completed: Boolean = false
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
            repo.allHikeStamps.asFlow(),
            repo.hikeDates.asFlow()
        ) { segments, allStamps, hikeStamps, hikeDates ->
            val dates = hikeDates.associate { it.id to it.date }
            val bySegment = allStamps.groupBy { it.segmentId }
            segments.map { seg ->
                val stamps = bySegment[seg.id] ?: emptyList()
                val progress = SegmentProgress.compute(stamps, hikeStamps) { dates[it].orEmpty() }
                SegmentRow(seg, progress.groups.size, progress.stampedGroupKeys.size, progress.complete)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}


