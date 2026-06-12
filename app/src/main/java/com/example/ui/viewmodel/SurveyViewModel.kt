package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.SurveyRepository
import com.example.data.model.SurveyLog
import com.example.data.model.SurveyReading
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SurveyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SurveyRepository

    val allLogs: StateFlow<List<SurveyLog>>
    
    private val _selectedLog = MutableStateFlow<SurveyLog?>(null)
    val selectedLog: StateFlow<SurveyLog?> = _selectedLog.asStateFlow()

    private val _selectedLogReadings = MutableStateFlow<List<SurveyReading>>(emptyList())
    val selectedLogReadings: StateFlow<List<SurveyReading>> = _selectedLogReadings.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SurveyRepository(database.surveyDao())
        
        allLogs = repository.allLogs
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun selectLog(log: SurveyLog?) {
        _selectedLog.value = log
        if (log != null) {
            viewModelScope.launch {
                repository.getReadingsForLog(log.id).collect { readings ->
                    _selectedLogReadings.value = readings
                }
            }
        } else {
            _selectedLogReadings.value = emptyList()
        }
    }

    fun createLog(
        name: String,
        operator: String,
        method: String,
        notes: String,
        spacingParam1: Double,
        spacingParam2: Double
    ) {
        viewModelScope.launch {
            val simpleDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            val newLog = SurveyLog(
                name = name,
                operator = operator,
                date = simpleDate,
                method = method,
                notes = notes,
                spacingParam1 = spacingParam1,
                spacingParam2 = spacingParam2
            )
            val logId = repository.insertLog(newLog)
            // Auto select newly created log
            val created = repository.getLogById(logId.toInt())
            if (created != null) {
                selectLog(created)
            }
        }
    }

    fun deleteLog(log: SurveyLog) {
        viewModelScope.launch {
            if (_selectedLog.value?.id == log.id) {
                selectLog(null)
            }
            repository.deleteLog(log)
        }
    }

    fun addReading(
        stationName: String,
        elevation: Double,
        latitude: Double,
        longitude: Double,
        value1: Double,
        value2: Double,
        apparentValue: Double,
        notes: String
    ) {
        val currentLog = _selectedLog.value ?: return
        viewModelScope.launch {
            val newReading = SurveyReading(
                logId = currentLog.id,
                stationName = stationName,
                elevation = elevation,
                latitude = latitude,
                longitude = longitude,
                value1 = value1,
                value2 = value2,
                apparentValue = apparentValue,
                notes = notes
            )
            repository.insertReading(newReading)
        }
    }

    fun deleteReading(reading: SurveyReading) {
        viewModelScope.launch {
            repository.deleteReading(reading)
        }
    }

    fun clearReadings(logId: Int) {
        viewModelScope.launch {
            repository.clearReadingsForLog(logId)
        }
    }
}
