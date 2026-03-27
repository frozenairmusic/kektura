package hu.kektura.app.ui.stamps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.kektura.app.data.SettingsStore
import hu.kektura.app.data.TrailType

@Composable
fun StampsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StampsViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTrail by remember { mutableStateOf<TrailType?>(null) }
    LaunchedEffect(Unit) {
        val selected = SettingsStore.getSelectedTrails(context)
        selectedTrail = selected.first()
        viewModel.selectedTrailTypes.value = selected.map { it.name }
    }

    val rows by viewModel.segmentRows.collectAsStateWithLifecycle()

    val total = rows.sumOf { it.stampCount }
    val collected = rows.sumOf { it.collectedCount }

    Column(modifier = modifier.fillMaxSize()) {
        // Progress header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            selectedTrail?.let { trail ->
                Text(
                    text = "${trail.name} – ${trail.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = "$collected / $total bélyegző",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            val progress by animateFloatAsState(
                targetValue = if (total > 0) collected.toFloat() / total else 0f,
                label = "progress"
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        HorizontalDivider()

        if (rows.isEmpty()) {
            Text(
                text = "Nincs elérhető bélyegző – a letöltés folyamatban van.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp
                )
            ) {
                items(rows, key = { it.segment.id }) { row ->
                    SegmentCard(row = row, onClick = { navController.navigate("stampDetail/${row.segment.id}") })
                }
            }
        }
    }
}

@Composable
private fun SegmentCard(
    row: SegmentRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allCollected = row.stampCount > 0 && row.collectedCount == row.stampCount
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allCollected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSegmentCode(row.segment.trailType, row.segment.id),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (row.stampCount > 0) {
                    Text(
                        text = "${row.collectedCount} / ${row.stampCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Letöltés folyamatban…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = row.segment.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = row.segment.region,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (row.segment.distanceKm > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                val info = buildString {
                    append("%.1f km".format(row.segment.distanceKm))
                    if (row.segment.elevationGainM > 0) {
                        append("  \u2191 ${row.segment.elevationGainM} m  \u2193 ${row.segment.elevationLossM} m")
                    }
                }
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (row.stampCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress by animateFloatAsState(
                    targetValue = row.collectedCount.toFloat() / row.stampCount,
                    label = "segmentProgress"
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

/**
 * Formats the segment code properly for each trail type instead of hardcoding OKT.
 */
private fun formatSegmentCode(trailType: String, id: Int): String {
    return when (trailType) {
        "OKT" -> "OKT-%02d".format(id)
        "RPDDK" -> "DDK-%02d".format(id - 200)
        "AK" -> "AK-%02d".format(id - 100)
        else -> "#%d".format(id)
    }
}
