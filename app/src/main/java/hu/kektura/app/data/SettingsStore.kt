package hu.kektura.app.data

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "kektura_settings"
private const val KEY_SELECTED_TRAILS = "selected_trails"

enum class TrailType(val displayName: String) {
    OKT("Országos Kéktúra"),
    DEL_DUNANTULI("Rockenbauer Pál Dél-Dunántúli Kéktúra"),
    ALFOLDI("Alföldi Kéktúra")
}

object SettingsStore {

    fun getSelectedTrails(context: Context): Set<TrailType> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val names = prefs.getStringSet(KEY_SELECTED_TRAILS, setOf(TrailType.OKT.name))
            ?: setOf(TrailType.OKT.name)
        val result = names.mapNotNull { runCatching { TrailType.valueOf(it) }.getOrNull() }.toSet()
        return result.ifEmpty { setOf(TrailType.OKT) }
    }

    fun setSelectedTrails(context: Context, trails: Set<TrailType>) {
        require(trails.isNotEmpty()) { "At least one trail must be selected" }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_SELECTED_TRAILS, trails.map { it.name }.toSet())
        }
    }
}
