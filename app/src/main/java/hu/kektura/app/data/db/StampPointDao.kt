package hu.kektura.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import hu.kektura.app.data.model.StampPoint

@Dao
interface StampPointDao {

    @Query("SELECT * FROM stamp_points ORDER BY id ASC")
    fun getAllLive(): LiveData<List<StampPoint>>

    @Query("SELECT * FROM stamp_points WHERE segmentId = :segId ORDER BY id ASC")
    fun getBySegmentIdLive(segId: Int): LiveData<List<StampPoint>>

    @Query("SELECT * FROM stamp_points")
    suspend fun getAll(): List<StampPoint>

    @Query("DELETE FROM stamp_points WHERE segmentId = :segId")
    suspend fun deleteBySegmentId(segId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<StampPoint>)
}
