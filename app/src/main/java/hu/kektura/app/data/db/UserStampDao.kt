package hu.kektura.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import hu.kektura.app.data.model.UserStamp

@Dao
interface UserStampDao {

    @Query("SELECT * FROM user_stamps ORDER BY collectedAt DESC")
    fun getAllLive(): LiveData<List<UserStamp>>

    @Query("SELECT stampPointId FROM user_stamps")
    fun getCollectedIdSetLive(): LiveData<List<Int>>

    @Query("SELECT COUNT(*) FROM user_stamps")
    fun countLive(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stamp: UserStamp)

    @Query("DELETE FROM user_stamps WHERE stampPointId = :pointId")
    suspend fun deleteByPointId(pointId: Int)
}
