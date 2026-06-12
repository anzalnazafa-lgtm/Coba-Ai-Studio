package com.example.data.database

import androidx.room.*
import com.example.data.model.SurveyLog
import com.example.data.model.SurveyReading
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Query("SELECT * FROM survey_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<SurveyLog>>

    @Query("SELECT * FROM survey_logs WHERE id = :logId LIMIT 1")
    suspend fun getLogById(logId: Int): SurveyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SurveyLog): Long

    @Delete
    suspend fun deleteLog(log: SurveyLog)

    @Query("SELECT * FROM survey_readings WHERE logId = :logId ORDER BY timestamp ASC")
    fun getReadingsForLog(logId: Int): Flow<List<SurveyReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SurveyReading): Long

    @Delete
    suspend fun deleteReading(reading: SurveyReading)

    @Query("DELETE FROM survey_readings WHERE logId = :logId")
    suspend fun clearReadingsForLog(logId: Int)
}
