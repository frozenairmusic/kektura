package hu.kektura.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One stamp collected during a [UserHike].
 * [stampPointId] references the physical stamp location (e.g. OKTPH_90_1).
 * [order] is the 1-based sequence within the hike (used for section completion).
 */
@Entity(
    tableName = "user_hike_stamps",
    foreignKeys = [
        ForeignKey(
            entity = UserHike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hikeId")]
)
data class UserHikeStamp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hikeId: Int,
    val stampPointId: Int,
    val order: Int,
    val collectedAt: String = "",
    val notes: String = ""
)
