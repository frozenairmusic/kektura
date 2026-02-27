package hu.kektura.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gpx_segments")
data class GpxSegment(
    @PrimaryKey val id: Int,
    val name: String,
    val region: String,
    val distanceKm: Float,
    val gpxContent: String? = null,
    val hasData: Boolean = false,
    val visible: Boolean = true,
    val lastUpdated: String? = null
)
