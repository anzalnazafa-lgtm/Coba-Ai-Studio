package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_logs")
data class SurveyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val operator: String,
    val date: String,
    val method: String, // e.g. "Resistivity (Wenner)", "Resistivity (Schlumberger)", "Geomagnetic"
    val notes: String,
    val spacingParam1: Double = 1.0, // e.g. spacer 'a' or current-electrode spacer 'AB/2'
    val spacingParam2: Double = 1.0  // e.g. spacer 'MN/2'
)
