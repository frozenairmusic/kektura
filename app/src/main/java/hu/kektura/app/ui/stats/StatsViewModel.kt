package hu.kektura.app.ui.stats

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
    val collectedGroups: Int
) {
    val completed: Boolean get() = totalGroups > 0 && collectedGroups >= totalGroups
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
            repo.collectedPointIds.asFlow()
        ) { segments, allStamps, collectedList ->
            val collectedIds = collectedList.toSet()
            val stampsBySegment = allStamps.groupBy { it.segmentId }

            val sectionStats = segments.map { seg ->
                val stamps = stampsBySegment[seg.id] ?: emptyList()
                val groups = stamps
                    .filter { it.stampCode.isNotBlank() }
                    .groupBy { groupKeyFor(it.stampCode) }
                val collectedGroups = groups.values.count { grp ->
                    grp.isNotEmpty() && grp.all { collectedIds.contains(it.id) }
                }
                SectionStat(seg, groups.size, collectedGroups)
            }

            val totalDistanceKm = segments.sumOf { it.distanceKm.toDouble() }.toFloat()
            val collectedDistanceKm = sectionStats
                .filter { it.completed }
                .sumOf { it.segment.distanceKm.toDouble() }
                .toFloat()

            val totalGain = segments.sumOf { it.elevationGainM }
            val totalLoss = segments.sumOf { it.elevationLossM }
            val collectedGain = sectionStats.filter { it.completed }.sumOf { it.segment.elevationGainM }
            val collectedLoss = sectionStats.filter { it.completed }.sumOf { it.segment.elevationLossM }

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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
