package com.example.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SessionRepository(private val dao: StressSessionDao) {
    val allSessions: Flow<List<StressSessionEntity>> = dao.getAllSessions()
    val sessionCount: Flow<Int> = dao.getSessionCount()
    val totalBytesBurned: Flow<Long?> = dao.getTotalBytesBurned()
    val allTimePeakSpeed: Flow<Double?> = dao.getAllTimePeakSpeed()

    suspend fun saveSession(session: StressSessionEntity): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSession(session: StressSessionEntity) {
        dao.deleteSession(session)
    }

    suspend fun deleteSessionById(id: Long) {
        dao.deleteSessionById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllSessions()
    }
}
