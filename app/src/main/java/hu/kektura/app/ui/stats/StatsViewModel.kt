package hu.kektura.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import hu.kektura.app.KekturaApp
import hu.kektura.app.data.model.GpxSegment
import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.util.SegmentProgress
import hu.kektura.app.util.TrailSections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** null means "All" */
enum class StatsFilter(val label: String, val trailTypes: List<String>?) {
    ALL("Összes", null),
    OKT("OKT", listOf("OKT")),
    RPDDK("RPDDK", listOf("RPDDK")),
    AK("AK", listOf("AK"))
}

data class SectionStat(
    val segment: GpxSegment,
    val totalGroups: Int,
    val collectedGroups: Int,
    /** True when same-date segment parts cover the whole segment (see SegmentProgress). */
    val completed: Boolean
) {
    val progress: Float get() = if (totalGroups == 0) 0f else collectedGroups.toFloat() / totalGroups
}

data class TrailStats(
    val totalDistanceKm: Float,
    val collectedDistanceKm: Float,
    val totalElevationGainM: Int,
    val collectedElevationGainM: Int,
    val totalElevationLossM: Int,
    val collectedElevationLossM: Int,
    val totalStampGroups: Int,
    val collectedStampGroups: Int,
    val sections: List<SectionStat>
) {
    val distanceProgress: Float
        get() = if (totalDistanceKm == 0f) 0f else collectedDistanceKm / totalDistanceKm
    val stampProgress: Float
        get() = if (totalStampGroups == 0) 0f else collectedStampGroups.toFloat() / totalStampGroups
    val sectionsDone: Int get() = sections.count { it.completed }
    val sectionsTotal: Int get() = sections.size
    val sectionsProgress: Float
        get() = if (sectionsTotal == 0) 0f else sectionsDone.toFloat() / sectionsTotal
    val elevationGainProgress: Float
        get() = if (totalElevationGainM == 0) 0f else collectedElevationGainM.toFloat() / totalElevationGainM
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as KekturaApp).repository

    val selectedFilter = MutableStateFlow(StatsFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<TrailStats?> =
        combine(
            selectedFilter.flatMapLatest { filter ->
                val types = filter.trailTypes ?: listOf("OKT", "RPDDK", "AK")
                repo.getSegmentsByTrailTypesLive(types).asFlow()
            },
            repo.allStampPoints.asFlow(),
            repo.allHikeStamps.asFlow(),
            repo.hikeDates.asFlow()
        ) { segments, allStamps, hikeStamps, hikeDates ->
            val dates = hikeDates.associate { it.id to it.date }
            val stampsBySegment = allStamps.groupBy { it.segmentId }
            val datesByCode = TrailSections.datesByCode(hikeStamps, allStamps)

            val sectionStats = segments.map { seg ->
                val stamps = stampsBySegment[seg.id] ?: emptyList()
                val progress = SegmentProgress.compute(stamps, hikeStamps) { dates[it].orEmpty() }
                SectionStat(
                    seg,
                    progress.groups.size,
                    progress.stampedGroupKeys.size,
                    progress.complete
                )
            }

            // Distance / elevation: what was actually walked (collected stretches of the track),
            // not all-or-nothing per segment. A fully collected segment counts in full.
            val totalDistanceKm = segments.sumOf { it.distanceKm.toDouble() }.toFloat()
            val totalGain = segments.sumOf { it.elevationGainM }
            val totalLoss = segments.sumOf { it.elevationLossM }
            var collectedDist = 0.0
            var collectedGainD = 0.0
            var collectedLossD = 0.0
            for (stat in sectionStats) {
                val seg = stat.segment
                val share = walkedShare(seg, stampsBySegment[seg.id].orEmpty(), datesByCode, stat.completed)
                collectedDist += seg.distanceKm * share.distance
                collectedGainD += seg.elevationGainM * share.gain
                collectedLossD += seg.elevationLossM * share.loss
            }
            val collectedDistanceKm = collectedDist.toFloat()
            val collectedGain = collectedGainD.toInt()
            val collectedLoss = collectedLossD.toInt()

            val totalGroups = sectionStats.sumOf { it.totalGroups }
            val collectedGroups = sectionStats.sumOf { it.collectedGroups }

            TrailStats(
                totalDistanceKm = totalDistanceKm,
                collectedDistanceKm = collectedDistanceKm,
                totalElevationGainM = totalGain,
                collectedElevationGainM = collectedGain,
                totalElevationLossM = totalLoss,
                collectedElevationLossM = collectedLoss,
                totalStampGroups = totalGroups,
                collectedStampGroups = collectedGroups,
                sections = sectionStats
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private class WalkedShare(val distance: Double, val gain: Double, val loss: Double)

    /**
     * Fraction (0..1) of a segment's distance / ascent / descent that lies on collected
     * stretches of its track. Ratios are applied to the segment's published totals so the
     * numbers stay consistent with the rest of the app.
     */
    private fun walkedShare(
        seg: GpxSegment,
        stamps: List<StampPoint>,
        datesByCode: Map<String, Set<String>>,
        complete: Boolean
    ): WalkedShare {
        if (complete) return WalkedShare(1.0, 1.0, 1.0)
        val sections = TrailSections.build(seg, stamps, datesByCode)
        if (sections.none { it.completed }) return WalkedShare(0.0, 0.0, 0.0)

        val totalLen = sections.sumOf { TrailSections.lengthKm(it.points) }
        val doneLen = sections.filter { it.completed }.sumOf { TrailSections.lengthKm(it.points) }
        val distance = if (totalLen > 0) (doneLen / totalLen).coerceIn(0.0, 1.0) else 0.0

        var gainAll = 0.0; var lossAll = 0.0; var gainDone = 0.0; var lossDone = 0.0
        for (sec in sections) {
            val (g, l) = TrailSections.gainLossM(sec.points)
            gainAll += g; lossAll += l
            if (sec.completed) { gainDone += g; lossDone += l }
        }
        // Tracks without <ele> data fall back to the distance share.
        val gain = if (gainAll > 0) (gainDone / gainAll).coerceIn(0.0, 1.0) else distance
        val loss = if (lossAll > 0) (lossDone / lossAll).coerceIn(0.0, 1.0) else distance
        return WalkedShare(distance, gain, loss)
    }
}
