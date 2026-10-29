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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.kektura.app.ui.theme.StampCollected
import hu.kektura.app.ui.theme.StampPending
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
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
    val segment by viewModel.segment.collectAsStateWithLifecycle()
    val collectSheet by viewModel.collectSheet.collectAsStateWithLifecycle()

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
            segment?.let { seg ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (seg.distanceKm > 0) {
                        Text(
                            text = "%.1f km".format(seg.distanceKm),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (seg.elevationGainM > 0) {
                        Text(
                            text = "\u2191 ${seg.elevationGainM} m  \u2193 ${seg.elevationLossM} m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
                            onOpenCollect = { viewModel.openCollectSheet(item.group.groupKey) }
                        )
                        is StampListItem.StampEntry -> StampEntryCard(
                            entry = item,
                            onOpenCollect = { viewModel.openCollectSheetForStamp(item.item.point.id) }
                        )
                    }
                }
            }
        }
    }

    // Collection bottom sheet — shown over the list when user taps the collect icon
    collectSheet?.let { sheet ->
        val isCollected = if (sheet.targetStampId != null) {
            list.filterIsInstance<StampListItem.StampEntry>()
                .find { it.item.point.id == sheet.targetStampId }?.item?.collected == true
        } else {
            headers.find { it.group.groupKey == sheet.groupKey }?.group?.allCollected == true
        }
        CollectSheet(
            state = sheet,
            isCollected = isCollected,
            onDateChange = viewModel::updateCollectDate,
            onNoteChange = viewModel::updateCollectNote,
            onConfirm = { viewModel.confirmCollect() },
            onRemoveAll = {
                if (sheet.targetStampId != null) viewModel.removeAllForStamp(sheet.targetStampId)
                else viewModel.removeAllForGroup(sheet.groupKey)
            },
            onDismiss = viewModel::dismissCollectSheet
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectSheet(
    state: CollectSheetState,
    isCollected: Boolean,
    onDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onRemoveAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = state.targetStampName.ifBlank { state.groupKey },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (state.targetStampId != null) "Egyedi bélyegző rögzítése"
                       else "Begyűjtés rögzítése",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // Date selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dátum",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = state.date, style = MaterialTheme.typography.bodyLarge)
                }
                TextButton(onClick = { showDatePicker = true }) { Text("Módosít") }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNoteChange,
                label = { Text("Megjegyzés (opcionális)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                Text("Begyűjt")
            }

            if (isCollected) {
                TextButton(
                    onClick = { onRemoveAll(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Bejegyzések törlése")
                }
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = dateStringToEpochMillis(state.date)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { onDateChange(epochMillisToDateString(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Mégse") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }
}

@Composable
private fun GroupHeaderCard(
    group: StampGroupUi,
    onToggle: () -> Unit,
    onOpenCollect: () -> Unit,
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

            // Collect status icon — click to open collect sheet
            // Tapping always opens the sheet: for first-time collection or to add another entry
            IconButton(onClick = onOpenCollect) {
                Icon(
                    imageVector = if (group.allCollected)
                        Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Begyűjtés kezelése",
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
    onOpenCollect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pt = entry.item.point

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .clickable(onClick = onOpenCollect),
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

// ── Date helpers ──────────────────────────────────────────────────────────────

private fun dateStringToEpochMillis(date: String): Long {
    val parts = date.split("-")
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    return cal.timeInMillis
}

private fun epochMillisToDateString(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}
