package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "survey_readings",
    foreignKeys = [
        ForeignKey(
            entity = SurveyLog::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["logId"])]
)
data class SurveyReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val logId: Int,
    val stationName: String, // e.g. "S1", "S2" or spacing distance
    val elevation: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val value1: Double = 0.0, // resistance R (Ohm) or magnetic reading (nT)
    val value2: Double = 0.0, // calculated K (m) or geomagnetic diurnal drift (nT)
    val apparentValue: Double = 0.0, // calculated Apparent Resistivity (Ohm.m) or corrected magnetic intensity (nT)
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
