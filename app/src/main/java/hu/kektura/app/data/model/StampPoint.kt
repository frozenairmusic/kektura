package hu.kektura.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stamp_points")
data class StampPoint(
    @PrimaryKey val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val region: String = "",
    val stampCode: String = "",
    val notes: String = "",
    val segmentId: Int,
    val elevation: Double = 0.0
)
