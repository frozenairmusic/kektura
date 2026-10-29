package hu.kektura.app.util

import hu.kektura.app.data.model.StampPoint
import hu.kektura.app.data.model.UserHikeStamp
import hu.kektura.app.data.repository.TrailRepository.Companion.groupKeyFor

/**
 * Computes which parts of a segment (e.g. OKT_21) have been walked.
 *
 * Terminology:
 *  - segment      : a whole OKT section; its stamp groups in trail order are g0..g(n-1)
 *  - leg i        : the stretch between group i and group i+1 (n-1 legs)
 *  - segment part : a run of >= 2 CONSECUTIVE groups (in trail order) that were all
 *                   stamped on the same date (= same hike). The order in which the
 *                   stamps were entered is irrelevant; a skipped group breaks the run.
 *
 * A segment is complete when its parts together cover every leg.
 * A group counts as stamped on a date when ANY of its points (_1, _2, ...) was stamped.
 */
object SegmentProgress {

    data class Group(val key: String, val points: List<StampPoint>) {
        val name: String get() = points.map { it.name }.distinct().joinToString(" / ")
    }

    data class Part(val date: String, val fromIndex: Int, val toIndex: Int) {
        val stampCount: Int get() = toIndex - fromIndex + 1
    }

    data class Result(
        val groups: List<Group>,
        val parts: List<Part>,
        /** Index i means the leg between groups[i] and groups[i+1]. */
        val coveredLegs: Set<Int>,
        /** Groups with at least one stamped point on any date. */
        val stampedGroupKeys: Set<String>
    ) {
        val legCount: Int get() = (groups.size - 1).coerceAtLeast(0)
        val complete: Boolean
            get() = when {
                groups.isEmpty() -> false
                groups.size == 1 -> groups[0].key in stampedGroupKeys
                else -> coveredLegs.size == legCount
            }
    }

    /** Ordered stamp groups of one segment (only points that have a stamp code). */
    fun groupsOf(points: List<StampPoint>): List<Group> {
        val ordered = points.filter { it.stampCode.isNotBlank() }.sortedBy { it.id }
        val byKey = LinkedHashMap<String, MutableList<StampPoint>>()
        for (p in ordered) byKey.getOrPut(groupKeyFor(p.stampCode).ifBlank { p.stampCode }) { mutableListOf() }.add(p)
        return byKey.map { (k, v) -> Group(k, v) }
    }

    /**
     * @param segmentPoints all stamp points of ONE segment
     * @param hikeStamps    hike stamp records (any hikes; filtered to this segment's points here)
     * @param dateOfHike    hikeId -> date string
     */
    fun compute(
        segmentPoints: List<StampPoint>,
        hikeStamps: List<UserHikeStamp>,
        dateOfHike: (Int) -> String
    ): Result {
        val groups = groupsOf(segmentPoints)
        val groupIndexByPoint = HashMap<Int, Int>()
        groups.forEachIndexed { gi, g -> g.points.forEach { groupIndexByPoint[it.id] = gi } }

        // hikeId -> set of group indices stamped in that hike
        val presentByHike = HashMap<Int, MutableSet<Int>>()
        val stamped = HashSet<String>()
        for (hs in hikeStamps) {
            val gi = groupIndexByPoint[hs.stampPointId] ?: continue
            presentByHike.getOrPut(hs.hikeId) { sortedSetOf() }.add(gi)
            stamped.add(groups[gi].key)
        }

        val parts = mutableListOf<Part>()
        val covered = sortedSetOf<Int>()
        for ((hikeId, present) in presentByHike) {
            val sorted = present.sorted()
            var runStart = sorted.firstOrNull() ?: continue
            var prev = runStart
            fun close(end: Int) {
                if (end > runStart) {
                    parts.add(Part(dateOfHike(hikeId), runStart, end))
                    for (leg in runStart until end) covered.add(leg)
                }
            }
            for (idx in sorted.drop(1)) {
                if (idx != prev + 1) { close(prev); runStart = idx }
                prev = idx
            }
            close(prev)
        }
        parts.sortWith(compareBy({ it.date }, { it.fromIndex }))
        return Result(groups, parts, covered, stamped)
    }
}
