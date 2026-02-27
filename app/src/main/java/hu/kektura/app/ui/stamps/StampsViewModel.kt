package hu.kektura.app.ui.stamps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor

data class SegmentRow(
    val segment: GpxSegment,
    val stampCount: Int,
    val collectedCount: Int
)

class StampsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    /**
     * All 27 OKT segments enriched with live stamp/collected counts.
     * Deduplicates by OKTPH group key exactly as in the original app.
     */
    val segmentRows: MediatorLiveData<List<SegmentRow>> =
        MediatorLiveData<List<SegmentRow>>().apply {
            var segments: List<GpxSegment> = emptyList()
            var allStamps: List<StampPoint> = emptyList()
            var collectedIds: Set<Int> = emptySet()

            fun merge() {
                val bySegment = allStamps.groupBy { it.segmentId }
                value = segments.map { seg ->
                    val stamps = bySegment[seg.id] ?: emptyList()
                    val groups = stamps.groupBy { sp ->
                        groupKeyFor(sp.stampCode).ifBlank { sp.stampCode }
                    }
                    val collected = groups.values.count { grp ->
                        grp.isNotEmpty() && grp.all { collectedIds.contains(it.id) }
                    }
                    SegmentRow(seg, groups.size, collected)
                }
            }

            addSource(repo.allSegments)       { segs -> segments    = segs;         merge() }
            addSource(repo.allStampPoints)    { pts  -> allStamps   = pts;          merge() }
            addSource(repo.collectedPointIds) { ids  -> collectedIds = ids.toSet(); merge() }
        }

    val totalCollected: LiveData<Int> = repo.collectedCount
}
