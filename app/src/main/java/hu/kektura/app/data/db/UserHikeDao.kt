package hu.kektura.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hu.kektura.app.data.model.UserHike
import hu.kektura.app.data.model.UserHikeStamp

@Dao
interface UserHikeDao {

    // ── Hike CRUD ─────────────────────────────────────────────────────────────

    @Insert
    suspend fun insertHike(hike: UserHike): Long

    @Update
    suspend fun updateHike(hike: UserHike)

    @Delete
    suspend fun deleteHike(hike: UserHike)

    @Query("SELECT * FROM user_hikes ORDER BY id DESC")
    fun getAllHikesLive(): LiveData<List<UserHike>>

    /** Reactive: emits the single active hike or null. */
    @Query("SELECT * FROM user_hikes WHERE status = 'active' ORDER BY id DESC LIMIT 1")
    fun getActiveHikeLive(): LiveData<UserHike?>

    /** Suspending point-in-time read used inside transactions. */
    @Query("SELECT * FROM user_hikes WHERE status = 'active' ORDER BY id DESC LIMIT 1")
    suspend fun getActiveHike(): UserHike?

    // ── Stamp CRUD ────────────────────────────────────────────────────────────

    @Insert
    suspend fun insertHikeStamp(stamp: UserHikeStamp)

    @Query("DELETE FROM user_hike_stamps WHERE hikeId = :hikeId AND stampPointId = :stampPointId")
    suspend fun removeStampFromHike(hikeId: Int, stampPointId: Int)

    /**
     * All stamps across all hikes, ordered for section-completion computation.
     * Grouping by hikeId and sorting by order gives each hike's sequence.
     */
    @Query("SELECT * FROM user_hike_stamps ORDER BY hikeId ASC, `order` ASC")
    fun getAllHikeStampsLive(): LiveData<List<UserHikeStamp>>

    /** Distinct stamp point IDs from all hikes — drives dot colours on the map. */
    @Query("SELECT DISTINCT stampPointId FROM user_hike_stamps")
    fun getCollectedStampIdsLive(): LiveData<List<Int>>

    /** Highest order value for a given hike (null if no stamps yet). */
    @Query("SELECT MAX(`order`) FROM user_hike_stamps WHERE hikeId = :hikeId")
    suspend fun getMaxOrderForHike(hikeId: Int): Int?

    /** Find a hike by exact date string "yyyy-MM-dd". */
    @Query("SELECT * FROM user_hikes WHERE date = :date LIMIT 1")
    suspend fun getHikeByDate(date: String): UserHike?

    /** Check if a stamp already exists in a given hike (duplicate guard). */
    @Query("SELECT * FROM user_hike_stamps WHERE hikeId = :hikeId AND stampPointId = :stampPointId LIMIT 1")
    suspend fun getStampInHike(hikeId: Int, stampPointId: Int): UserHikeStamp?

    /** Delete every hike stamp record for a physical stamp point. */
    @Query("DELETE FROM user_hike_stamps WHERE stampPointId = :stampPointId")
    suspend fun deleteAllStampsForPoint(stampPointId: Int)

    /** Remove hikes that have no stamps left. */
    @Query("DELETE FROM user_hikes WHERE id NOT IN (SELECT DISTINCT hikeId FROM user_hike_stamps)")
    suspend fun deleteEmptyHikes()
}
