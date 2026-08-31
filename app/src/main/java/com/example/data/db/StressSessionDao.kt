package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StressSessionDao {
    @Query("SELECT * FROM stress_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<StressSessionEntity>>

    @Query("SELECT * FROM stress_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): StressSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StressSessionEntity): Long

    @Delete
    suspend fun deleteSession(session: StressSessionEntity)

    @Query("DELETE FROM stress_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM stress_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT COUNT(*) FROM stress_sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT SUM(totalBytesDownloaded + totalBytesUploaded) FROM stress_sessions")
    fun getTotalBytesBurned(): Flow<Long?>

    @Query("SELECT MAX(peakSpeedMbps) FROM stress_sessions")
    fun getAllTimePeakSpeed(): Flow<Double?>
}
