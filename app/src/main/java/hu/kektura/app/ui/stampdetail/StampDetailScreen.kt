package hu.kektura.app.ui.stampdetail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.kektura.app.ui.theme.StampCollected
import hu.kektura.app.ui.theme.StampPending

@Composable
fun StampDetailScreen(
    segmentId: Int,
    modifier: Modifier = Modifier,
    viewModel: StampDetailViewModel = viewModel(
        factory = StampDetailViewModel.Factory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application,
            segmentId
        )
    )
) {
    val list by viewModel.groupedList.collectAsStateWithLifecycle()

    val headers = list.filterIsInstance<StampListItem.GroupHeader>()
    val total = headers.size
    val collected = headers.count { it.group.allCollected }

    Column(modifier = modifier.fillMaxSize()) {
        // Progress header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "$collected / $total bélyegző",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            val progress by animateFloatAsState(
                targetValue = if (total > 0) collected.toFloat() / total else 0f,
                label = "detailProgress"
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        HorizontalDivider()

        if (list.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp
                )
            ) {
                items(list, key = { item ->
                    when (item) {
                        is StampListItem.GroupHeader -> "group_${item.group.groupKey}"
                        is StampListItem.StampEntry -> "stamp_${item.item.point.id}"
                    }
                }) { item ->
                    when (item) {
                        is StampListItem.GroupHeader -> GroupHeaderCard(
                            group = item.group,
                            onToggle = { viewModel.toggleGroup(item.group.groupKey) },
                            onCollect = { viewModel.collectGroup(item.group.groupKey) },
                            onRemove = { viewModel.removeGroup(item.group.groupKey) }
                        )
                        is StampListItem.StampEntry -> StampEntryCard(
                            entry = item
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeaderCard(
    group: StampGroupUi,
    onToggle: () -> Unit,
    onCollect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.groupKey,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (group.groupName.isNotBlank()) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Collect/uncollect button
            IconButton(onClick = { if (group.allCollected) onRemove() else onCollect() }) {
                Icon(
                    imageVector = if (group.allCollected)
                        Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (group.allCollected) "Visszavonás" else "Begyűjtés",
                    tint = if (group.allCollected) StampCollected else StampPending,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Expand/collapse chevron
            val rotation by animateFloatAsState(
                targetValue = if (group.expanded) 180f else 0f,
                label = "chevron"
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "Kinyitás / összecsukás",
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun StampEntryCard(
    entry: StampListItem.StampEntry,
    modifier: Modifier = Modifier
) {
    val pt = entry.item.point

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (entry.item.collected)
                    Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (entry.item.collected) "Begyűjtve" else "Hiányzik",
                tint = if (entry.item.collected) StampCollected else StampPending,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pt.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val subCode = if (pt.stampCode.isNotBlank() && pt.stampCode != entry.groupKey)
                        pt.stampCode.removePrefix("${entry.groupKey}_") else ""
                    if (subCode.isNotBlank()) {
                        Text(
                            text = subCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (pt.elevation > 0.0) {
                        Text(
                            text = "%.0f m".format(pt.elevation),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (pt.notes.isNotBlank()) {
                    Text(
                        text = pt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
