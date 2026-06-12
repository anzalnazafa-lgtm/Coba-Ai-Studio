package com.example.data.database

import com.example.data.model.SurveyLog
import com.example.data.model.SurveyReading
import kotlinx.coroutines.flow.Flow

class SurveyRepository(private val surveyDao: SurveyDao) {
    val allLogs: Flow<List<SurveyLog>> = surveyDao.getAllLogs()

    suspend fun getLogById(logId: Int): SurveyLog? = surveyDao.getLogById(logId)

    suspend fun insertLog(log: SurveyLog): Long = surveyDao.insertLog(log)

    suspend fun deleteLog(log: SurveyLog) = surveyDao.deleteLog(log)

    fun getReadingsForLog(logId: Int): Flow<List<SurveyReading>> = surveyDao.getReadingsForLog(logId)

    suspend fun insertReading(reading: SurveyReading): Long = surveyDao.insertReading(reading)

    suspend fun deleteReading(reading: SurveyReading) = surveyDao.deleteReading(reading)

    suspend fun clearReadingsForLog(logId: Int) = surveyDao.clearReadingsForLog(logId)
}
