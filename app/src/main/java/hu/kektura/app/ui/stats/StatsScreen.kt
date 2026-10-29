package hu.kektura.app.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val ColorCompleted = Color(0xFFFF7043)
private val ColorPending   = Color(0xFF1565C0)

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Text(
                "Statisztika",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(StatsFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.selectedFilter.value = filter },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorPending,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (stats == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            return@LazyColumn
        }

        val s = stats!!

        // ── Big four stat cards ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RingStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Távolság",
                    value = "%.1f".format(s.collectedDistanceKm),
                    total = "/ %.0f km".format(s.totalDistanceKm),
                    progress = s.distanceProgress,
                    unit = "km"
                )
                RingStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Bélyegzők",
                    value = "${s.collectedStampGroups}",
                    total = "/ ${s.totalStampGroups}",
                    progress = s.stampProgress,
                    unit = "db"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevationCard(
                    modifier = Modifier.weight(1f),
                    label = "Szintemelkedés",
                    collectedM = s.collectedElevationGainM,
                    totalM = s.totalElevationGainM,
                    isGain = true
                )
                ElevationCard(
                    modifier = Modifier.weight(1f),
                    label = "Szintereszkedés",
                    collectedM = s.collectedElevationLossM,
                    totalM = s.totalElevationLossM,
                    isGain = false
                )
            }
        }

        // ── Sections header ───────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Szakaszok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${s.sectionsDone} / ${s.sectionsTotal}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (s.sectionsDone > 0) ColorCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            AnimatedLinearBar(progress = s.sectionsProgress)
        }

        // ── Per-section rows ──────────────────────────────────────────────────
        items(s.sections) { sec ->
            SectionRow(sec, distanceFraction = sec.progress)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun RingStatCard(
    label: String,
    value: String,
    total: String,
    progress: Float,
    unit: String,
    modifier: Modifier = Modifier
) {
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animTarget = progress }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(800),
        label = "ring"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier.size(80.dp),
                    color = if (animProgress >= 1f) ColorCompleted else ColorPending,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                total,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ElevationCard(
    label: String,
    collectedM: Int,
    totalM: Int,
    isGain: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalM == 0) 0f else collectedM.toFloat() / totalM
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animTarget = progress }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(800),
        label = "elev"
    )
    val arrow = if (isGain) "↑" else "↓"

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    arrow,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isGain) ColorCompleted else ColorPending,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "%,d m".format(collectedM),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "/ %,d m".format(totalM),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedLinearBar(progress = animProgress)
        }
    }
}

@Composable
private fun SectionRow(sec: SectionStat, distanceFraction: Float) {
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(distanceFraction) { animTarget = distanceFraction }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(600),
        label = "sec"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (sec.completed) ColorCompleted else ColorPending)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sec.segment.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (sec.completed) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ColorCompleted,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = ColorPending.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "${sec.collectedGroups}/${sec.totalGroups}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sec.completed) ColorCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (sec.totalGroups > 0) {
                    LinearProgressIndicator(
                        progress = { animProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (sec.completed) ColorCompleted else ColorPending,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round
                    )
                }

                val travelledKm = sec.segment.distanceKm * sec.progress
                val travelledGain = (sec.segment.elevationGainM * sec.progress).toInt()
                val travelledLoss = (sec.segment.elevationLossM * sec.progress).toInt()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "%.1f / %.1f km".format(travelledKm, sec.segment.distanceKm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sec.segment.elevationGainM > 0) {
                        Text(
                            "↑ %,d / %,d m".format(travelledGain, sec.segment.elevationGainM),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (sec.segment.elevationLossM > 0) {
                        Text(
                            "↓ %,d / %,d m".format(travelledLoss, sec.segment.elevationLossM),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedLinearBar(progress: Float, modifier: Modifier = Modifier) {
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animTarget = progress }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(800),
        label = "bar"
    )
    LinearProgressIndicator(
        progress = { animProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = if (animProgress >= 1f) ColorCompleted else ColorPending,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeCap = StrokeCap.Round
    )
}
