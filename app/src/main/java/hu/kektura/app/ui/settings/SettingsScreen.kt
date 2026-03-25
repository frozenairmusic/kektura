package hu.kektura.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import hu.kektura.app.data.SettingsStore
import hu.kektura.app.data.TrailType

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTrail by remember {
        mutableStateOf(SettingsStore.getSelectedTrails(context).first())
    }

    val trails = listOf(
        TrailType.OKT to "Országos Kéktúra",
        TrailType.RPDDK to "Rockenbauer Pál Dél-Dunántúli Kéktúra",
        TrailType.AK to "Alföldi Kéktúra"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Túraútvonalak",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Válassz egyet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        trails.forEachIndexed { index, (trailType, label) ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            ListItem(
                headlineContent = {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                },
                leadingContent = {
                    RadioButton(
                        selected = trailType == selectedTrail,
                        onClick = {
                            selectedTrail = trailType
                            SettingsStore.setSelectedTrails(context, setOf(trailType))
                        }
                    )
                }
            )
        }
    }
}
