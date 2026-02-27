package hu.kektura.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import hu.kektura.app.data.model.GpxSegment

@Dao
interface GpxSegmentDao {

    @Query("SELECT * FROM gpx_segments ORDER BY id ASC")
    fun getAllLive(): LiveData<List<GpxSegment>>

    @Query("SELECT * FROM gpx_segments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GpxSegment?

    @Query("UPDATE gpx_segments SET gpxContent = :gpx, hasData = 1, lastUpdated = :ts WHERE id = :id")
    suspend fun updateGpxContent(id: Int, gpx: String, ts: String)

    @Query("UPDATE gpx_segments SET gpxContent = NULL, hasData = 0, lastUpdated = NULL WHERE id = :id")
    suspend fun clearGpxContent(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(segments: List<GpxSegment>)
}
