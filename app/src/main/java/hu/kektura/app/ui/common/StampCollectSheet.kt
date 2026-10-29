package hu.kektura.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.kektura.app.ui.theme.StampCollected
import hu.kektura.app.ui.theme.StampPending
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * One date on which a stamp (or a group of stamps) was collected.
 * [entryIds] are the underlying user_hike_stamps rows; [label] names the group members
 * collected that day (e.g. "_1, _2") and is blank for single stamps.
 */
data class StampHistoryRow(
    val date: String,
    val notes: String,
    val entryIds: List<Int>,
    val label: String = ""
)

/**
 * Shared collect sheet used by the map and the stamps screen: shows every date the stamp
 * was collected (editable / deletable) and lets the user add a collection for any past date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StampCollectSheet(
    title: String,
    rows: List<StampHistoryRow>,
    error: String?,
    onAdd: (date: String, notes: String) -> Unit,
    onEdit: (row: StampHistoryRow, date: String, notes: String) -> Unit,
    onDelete: (row: StampHistoryRow) -> Unit,
    onDeleteAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    header: @Composable ColumnScope.() -> Unit = {}
) {
    var newDate by remember { mutableStateOf(todayString()) }
    var newNotes by remember { mutableStateOf("") }
    var editingKey by remember { mutableStateOf<String?>(null) }
    var editDate by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var deleteRow by remember { mutableStateOf<StampHistoryRow?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            header()

            Text(
                text = if (rows.isEmpty()) "Még nincs begyűjtve"
                else "Begyűjtve · ${rows.size} alkalom",
                style = MaterialTheme.typography.labelLarge,
                color = if (rows.isEmpty()) StampPending else StampCollected
            )

            // ── History ──────────────────────────────────────────────────────
            rows.forEach { row ->
                val key = row.date
                if (editingKey == key) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateRow(editDate) { pickerTarget = PickerTarget.EDIT }
                            OutlinedTextField(
                                value = editNotes,
                                onValueChange = { editNotes = it },
                                label = { Text("Megjegyzés") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 1,
                                maxLines = 4
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { editingKey = null }) { Text("Mégse") }
                                Button(onClick = {
                                    onEdit(row, editDate, editNotes)
                                    editingKey = null
                                }) { Text("Mentés") }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = row.date,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (row.label.isNotBlank()) {
                                Text(
                                    text = row.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (row.notes.isNotBlank()) {
                                Text(
                                    text = row.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = {
                            editingKey = key
                            editDate = row.date
                            editNotes = row.notes
                        }) { Icon(Icons.Default.Edit, contentDescription = "Szerkesztés") }
                        IconButton(onClick = { deleteRow = row }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Törlés",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()

            // ── Add a collection ─────────────────────────────────────────────
            Text(
                text = "Új begyűjtés",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DateRow(newDate) { pickerTarget = PickerTarget.NEW }
            OutlinedTextField(
                value = newNotes,
                onValueChange = { newNotes = it },
                label = { Text("Megjegyzés (opcionális)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 4
            )
            Button(
                onClick = {
                    onAdd(newDate, newNotes)
                    newNotes = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Begyűjt") }

            if (rows.isNotEmpty()) {
                TextButton(
                    onClick = { confirmDeleteAll = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Összes bejegyzés törlése") }
            }
        }
    }

    pickerTarget?.let { target ->
        StampDatePickerDialog(
            initial = if (target == PickerTarget.NEW) newDate else editDate,
            onPick = { picked -> if (target == PickerTarget.NEW) newDate = picked else editDate = picked },
            onDismiss = { pickerTarget = null }
        )
    }

    deleteRow?.let { row ->
        AlertDialog(
            onDismissRequest = { deleteRow = null },
            title = { Text("Bejegyzés törlése") },
            text = { Text("Törlöd a(z) ${row.date} dátumú begyűjtést?") },
            confirmButton = {
                TextButton(onClick = { onDelete(row); deleteRow = null }) { Text("Törlés") }
            },
            dismissButton = { TextButton(onClick = { deleteRow = null }) { Text("Mégse") } }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Összes bejegyzés törlése") },
            text = { Text("Minden dátum törlődik ennél a bélyegzőnél.") },
            confirmButton = {
                TextButton(onClick = { onDeleteAll(); confirmDeleteAll = false }) { Text("Törlés") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text("Mégse") } }
        )
    }
}

private enum class PickerTarget { NEW, EDIT }

@Composable
private fun DateRow(date: String, onChange: () -> Unit) {
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
            Text(text = date, style = MaterialTheme.typography.bodyLarge)
        }
        TextButton(onClick = onChange) { Text("Módosít") }
    }
}

/** Date picker that does not allow future dates. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StampDatePickerDialog(
    initial: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = remember { dateStringToEpochMillis(todayString()) }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToEpochMillis(initial),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(epochMillisToDateString(it)) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Mégse") } }
    ) {
        DatePicker(state = state)
    }
}

// ── Date helpers ──────────────────────────────────────────────────────────────

fun todayString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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
