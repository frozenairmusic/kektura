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

    @Query("DELETE FROM stamp_points WHERE segmentId = :segId")
    suspend fun deleteBySegmentId(segId: Int)

    /** Every stamp point (in any segment) that carries exactly this stamp code. */
    @Query("SELECT id FROM stamp_points WHERE stampCode = :code AND stampCode != ''")
    suspend fun getIdsByStampCode(code: String): List<Int>

    @Query("SELECT stampCode FROM stamp_points WHERE id = :id")
    suspend fun getStampCode(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<StampPoint>)
}
