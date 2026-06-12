package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurveyReading
import kotlin.math.log10
import kotlin.math.pow

enum class ChartMode {
    LINEAR_PROFILE,  // Linear X (Station index) vs Linear Y (Value)
    LOG_LOG_SOUNDING, // Log10 X (Spacing distance) vs Log10 Y (Apparent Resistivity)
    COORDINATE_MAP   // Scatter Plot: Latitude vs Longitude representation
}

@Composable
fun VisualProfileChart(
    readings: List<SurveyReading>,
    valueSelector: (SurveyReading) -> Double,
    yLabel: String,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ChartMode.LINEAR_PROFILE) }

    if (readings.isEmpty()) {
        Box(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No data",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Belum ada stasiun data untuk grafis survei",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val accentColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val mapMarkerColor = Color(0xFFE53935)

    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // Tab Header selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedMode) {
                    ChartMode.LINEAR_PROFILE -> "Profil Survei ($yLabel)"
                    ChartMode.LOG_LOG_SOUNDING -> "Sounding VES (Kurva Log-Log)"
                    ChartMode.COORDINATE_MAP -> "Peta Sebaran Lintas Koordinat"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic mini buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { selectedMode = ChartMode.LINEAR_PROFILE },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (selectedMode == ChartMode.LINEAR_PROFILE) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Linear",
                        tint = if (selectedMode == ChartMode.LINEAR_PROFILE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { selectedMode = ChartMode.LOG_LOG_SOUNDING },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (selectedMode == ChartMode.LOG_LOG_SOUNDING) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Log-Log",
                        tint = if (selectedMode == ChartMode.LOG_LOG_SOUNDING) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { selectedMode = ChartMode.COORDINATE_MAP },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (selectedMode == ChartMode.COORDINATE_MAP) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map Plot",
                        tint = if (selectedMode == ChartMode.COORDINATE_MAP) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
                val width = size.width
                val height = size.height

                when (selectedMode) {
                    ChartMode.LINEAR_PROFILE -> {
                        // Linear charting
                        val points = readings.mapIndexed { index, reading ->
                            index to valueSelector(reading)
                        }

                        val minY = points.minOf { it.second }
                        val maxY = points.maxOf { it.second }
                        val minX = 0f
                        val maxX = (points.size - 1).toFloat()

                        val yRange = if (maxY == minY) 1.0 else (maxY - minY)
                        val xRange = if (maxX == minX) 1f else maxX - minX

                        fun getCanvasX(index: Int): Float =
                            if (maxX == minX) width / 2 else index * (width / xRange)

                        fun getCanvasY(value: Double): Float {
                            val ratio = (value - minY) / yRange
                            return (height - (ratio * height)).toFloat()
                        }

                        // Draw Linear Grid (Horizontal & Vertical)
                        val numGridLinesY = 4
                        for (i in 0..numGridLinesY) {
                            val yPct = i.toFloat() / numGridLinesY
                            val yPos = height * yPct
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, yPos),
                                end = Offset(width, yPos),
                                strokeWidth = 1.dp.toPx()
                            )

                            val labelValue = maxY - (yPct * yRange)
                            val labelString = String.format("%.1f", labelValue)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = labelString,
                                topLeft = Offset(-24.dp.toPx(), yPos - 10f),
                                style = androidx.compose.ui.text.TextStyle(
                                    color = onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 7.sp
                                )
                            )
                        }

                        // Draw Curve path
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(getCanvasX(points[0].first), getCanvasY(points[0].second))
                                for (i in 1 until points.size) {
                                    lineTo(getCanvasX(points[i].first), getCanvasY(points[i].second))
                                }
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw Point dots
                        points.forEachIndexed { i, pt ->
                            val cx = getCanvasX(pt.first)
                            val cy = getCanvasY(pt.second)

                            drawCircle(color = accentColor, radius = 5.dp.toPx(), center = Offset(cx, cy))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(cx, cy))

                            // Draw Station labels on X bottom
                            if (points.size <= 15) {
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = readings[i].stationName,
                                    topLeft = Offset(cx - 10f, height + 4f),
                                    style = androidx.compose.ui.text.TextStyle(
                                        color = onSurfaceVariant,
                                        fontSize = 8.sp
                                    )
                                )
                            }
                        }
                    }

                    ChartMode.LOG_LOG_SOUNDING -> {
                        // Sounding curves (Log-Log VES scale)
                        // Useful x-axis value: value1 (Spacing parameter / AB/2 for resistivity)
                        // Useful y-axis value: apparentValue (Apparent resistivity)

                        val dataPoints = readings.map { r ->
                            val xVal = if (r.value1 <= 0.0) 1.0 else r.value1
                            val yVal = if (r.apparentValue <= 0.0) 0.1 else r.apparentValue
                            xVal to yVal
                        }

                        val minXVal = dataPoints.minOf { it.first }
                        val maxXVal = dataPoints.maxOf { it.first }
                        val minYVal = dataPoints.minOf { it.second }
                        val maxYVal = dataPoints.maxOf { it.second }

                        // Decades range computation
                        val logMinX = log10(minXVal).coerceAtMost(0.0) // floor power of 10
                        val logMaxX = log10(maxXVal).coerceAtLeast(1.0)
                        val logMinY = log10(minYVal).coerceAtMost(0.0)
                        val logMaxY = log10(maxYVal).coerceAtLeast(1.0)

                        val floorLogX = logMinX.toInt() - 1
                        val ceilLogX = logMaxX.toInt() + 2
                        val floorLogY = logMinY.toInt() - 1
                        val ceilLogY = logMaxY.toInt() + 2

                        val logXRange = (ceilLogX - floorLogX).toDouble()
                        val logYRange = (ceilLogY - floorLogY).toDouble()

                        fun getLogXCanvas(originalX: Double): Float {
                            val logVal = log10(originalX)
                            val ratio = (logVal - floorLogX) / logXRange
                            return (ratio * width).toFloat()
                        }

                        fun getLogYCanvas(originalY: Double): Float {
                            val logVal = log10(originalY)
                            val ratio = (logVal - floorLogY) / logYRange
                            // flip visual Y
                            return (height - (ratio * height)).toFloat()
                        }

                        // Draw exponential decades grid lines
                        // Horizontal decades (Y axis)
                        for (dec in floorLogY..ceilLogY) {
                            val valAtDec = 10.0.pow(dec)
                            val yPos = getLogYCanvas(valAtDec)

                            if (yPos >= 0f && yPos <= height) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.25f),
                                    start = Offset(0f, yPos),
                                    end = Offset(width, yPos),
                                    strokeWidth = 1.3.dp.toPx()
                                )

                                val labelStr = "10^$dec"
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelStr,
                                    topLeft = Offset(-24.dp.toPx(), yPos - 10f),
                                    style = androidx.compose.ui.text.TextStyle(
                                        color = onSurfaceVariant,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Draw subdecades (2 to 9)
                                for (sub in 2..9) {
                                    val subVal = sub * valAtDec
                                    val subY = getLogYCanvas(subVal)
                                    if (subY >= 0f && subY <= height) {
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(0f, subY),
                                            end = Offset(width, subY),
                                            strokeWidth = 0.5.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }

                        // Vertical decades (X axis)
                        for (dec in floorLogX..ceilLogX) {
                            val valAtDec = 10.0.pow(dec)
                            val xPos = getLogXCanvas(valAtDec)

                            if (xPos >= 0f && xPos <= width) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.25f),
                                    start = Offset(xPos, 0f),
                                    end = Offset(xPos, height),
                                    strokeWidth = 1.3.dp.toPx()
                                )

                                val labelStr = "${valAtDec.toInt()}"
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelStr,
                                    topLeft = Offset(xPos - 12f, height + 4f),
                                    style = androidx.compose.ui.text.TextStyle(
                                        color = onSurfaceVariant,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Draw micro-steps
                                for (sub in 2..9) {
                                    val subVal = sub * valAtDec
                                    val subX = getLogXCanvas(subVal)
                                    if (subX >= 0f && subX <= width) {
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(subX, 0f),
                                            end = Offset(subX, height),
                                            strokeWidth = 0.5.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }

                        // Draw Curve Sounding line
                        if (dataPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(getLogXCanvas(dataPoints[0].first), getLogYCanvas(dataPoints[0].second))
                                for (i in 1 until dataPoints.size) {
                                    lineTo(getLogXCanvas(dataPoints[i].first), getLogYCanvas(dataPoints[i].second))
                                }
                            }
                            drawPath(
                                path = path,
                                color = secondaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw Point dots
                        dataPoints.forEachIndexed { i, pt ->
                            val cx = getLogXCanvas(pt.first)
                            val cy = getLogYCanvas(pt.second)

                            drawCircle(color = accentColor, radius = 5.dp.toPx(), center = Offset(cx, cy))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(cx, cy))
                        }
                    }

                    ChartMode.COORDINATE_MAP -> {
                        // Scatter plot: Latitude vs Longitude representation of profile points
                        val coords = readings.map { r ->
                            r.longitude to r.latitude
                        }

                        val minX = coords.minOf { it.first }
                        val maxX = coords.maxOf { it.first }
                        val minY = coords.minOf { it.second }
                        val maxY = coords.maxOf { it.second }

                        val xRange = if (maxX == minX) 1.0 else (maxX - minX)
                        val yRange = if (maxY == minY) 1.0 else (maxY - minY)

                        fun getMapX(lon: Double): Float =
                            if (maxX == minX) width / 2 else ((lon - minX) / xRange * width).toFloat()

                        fun getMapY(lat: Double): Float =
                            if (maxY == minY) height / 2 else (height - ((lat - minY) / yRange * height)).toFloat()

                        // Draw connection trace
                        if (coords.size > 1) {
                            val path = Path().apply {
                                moveTo(getMapX(coords[0].first), getMapY(coords[0].second))
                                for (i in 1 until coords.size) {
                                    lineTo(getMapX(coords[i].first), getMapY(coords[i].second))
                                }
                            }
                            drawPath(
                                path = path,
                                color = primaryColor.copy(alpha = 0.5f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw scattered stasiun circles with Elevation labels
                        coords.forEachIndexed { i, pt ->
                            val cx = getMapX(pt.first)
                            val cy = getMapY(pt.second)

                            drawCircle(
                                color = mapMarkerColor,
                                radius = 6.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(cx, cy)
                            )

                            // Title name and Elevation label
                            val station = readings[i]
                            val mapLabel = "${station.stationName} (${station.elevation}m)"
                            drawText(
                                textMeasurer = textMeasurer,
                                text = mapLabel,
                                topLeft = Offset(cx + 8f, cy - 12f),
                                style = androidx.compose.ui.text.TextStyle(
                                    color = onSurfaceVariant,
                                    fontSize = 7.2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
