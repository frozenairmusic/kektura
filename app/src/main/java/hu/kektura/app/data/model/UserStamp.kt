package hu.kektura.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stamps")
data class UserStamp(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val stampPointId: Int,
    val collectedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
