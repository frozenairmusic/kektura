package hu.kektura.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single hiking trip. Stamps are recorded via [UserHikeStamp].
 * status = "active"   → hike in progress
 * status = "finished" → completed hike
 */
@Entity(tableName = "user_hikes")
data class UserHike(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,           // "2026-06-03"
    val notes: String = "",
    val status: String = "finished"
)
