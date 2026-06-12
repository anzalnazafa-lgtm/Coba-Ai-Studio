package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sqrt

enum class CalcType {
    WENNER, SCHLUMBERGER, SEISMIC, GEOMAGNETIC
}

@Composable
fun CalculatorScreen() {
    var selectedCalc by remember { mutableStateOf(CalcType.WENNER) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Kalkulator Geofisika",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Hitung parameter data lapangan geofisika secara instan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Selector row
        ScrollableTabRow(
            selectedTabIndex = selectedCalc.ordinal,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().testTag("calc_selector")
        ) {
            Tab(
                selected = selectedCalc == CalcType.WENNER,
                onClick = { selectedCalc = CalcType.WENNER },
                text = { Text("Wenner (Resistivity)") }
            )
            Tab(
                selected = selectedCalc == CalcType.SCHLUMBERGER,
                onClick = { selectedCalc = CalcType.SCHLUMBERGER },
                text = { Text("Schlumberger (VES)") }
            )
            Tab(
                selected = selectedCalc == CalcType.SEISMIC,
                onClick = { selectedCalc = CalcType.SEISMIC },
                text = { Text("Refraksi (Seismik)") }
            )
            Tab(
                selected = selectedCalc == CalcType.GEOMAGNETIC,
                onClick = { selectedCalc = CalcType.GEOMAGNETIC },
                text = { Text("Geomagnetik") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedCalc) {
            CalcType.WENNER -> WennerCalculator()
            CalcType.SCHLUMBERGER -> SchlumbergerCalculator()
            CalcType.SEISMIC -> SeismicCalculator()
            CalcType.GEOMAGNETIC -> GeomagneticCalculator()
        }
    }
}

@Composable
fun WennerCalculator() {
    var spacingText by remember { mutableStateOf("2.0") }
    var resistanceText by remember { mutableStateOf("15.5") }

    val spacing = spacingText.toDoubleOrNull() ?: 0.0
    val resistance = resistanceText.toDoubleOrNull() ?: 0.0

    val factorK = 2 * PI * spacing
    val rhoA = factorK * resistance

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Metode Tahanan Jenis (Wenner Array)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = spacingText,
                onValueChange = { spacingText = it },
                label = { Text("Spasi Elektroda (a) dalam Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("wenner_spacing_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = resistanceText,
                onValueChange = { resistanceText = it },
                label = { Text("Resistansi Terukur (R) dalam Ohm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("wenner_resistance_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResultDisplayField(
                factorK = factorK,
                rhoA = rhoA,
                formula = "K = 2 * π * a\nρ_a = K * R",
                units = "Ohm.meter"
            )
        }
    }
}

@Composable
fun SchlumbergerCalculator() {
    var ab2Text by remember { mutableStateOf("10.0") }
    var mn2Text by remember { mutableStateOf("1.0") }
    var resistanceText by remember { mutableStateOf("8.4") }

    val ab2 = ab2Text.toDoubleOrNull() ?: 0.0
    val mn2 = mn2Text.toDoubleOrNull() ?: 0.0
    val resistance = resistanceText.toDoubleOrNull() ?: 0.0

    // K = PI * ((AB/2)^2 - (MN/2)^2) / (MN)
    // where mn2 = MN/2, so MN = 2 * mn2
    val factorK = if (mn2 > 0.0) {
        PI * (ab2 * ab2 - mn2 * mn2) / (2 * mn2)
    } else 0.0
    val rhoA = factorK * resistance

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Metode Tahanan Jenis (Schlumberger Array)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ab2Text,
                onValueChange = { ab2Text = it },
                label = { Text("Spasi Arus Luar (AB/2) dalam Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("schlumberger_ab_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = mn2Text,
                onValueChange = { mn2Text = it },
                label = { Text("Spasi Potensial Dalam (MN/2) dalam Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("schlumberger_mn_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = resistanceText,
                onValueChange = { resistanceText = it },
                label = { Text("Resistansi Terukur (R) dalam Ohm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("schlumberger_resistance_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResultDisplayField(
                factorK = factorK,
                rhoA = rhoA,
                formula = "K = π * [ (AB/2)² - (MN/2)² ] / MN\nρ_a = K * R",
                units = "Ohm.meter"
            )
        }
    }
}

@Composable
fun SeismicCalculator() {
    var tiText by remember { mutableStateOf("0.045") } // seconds
    var v1Text by remember { mutableStateOf("800.0") } // wave speed layer 1
    var v2Text by remember { mutableStateOf("2200.0") } // wave speed layer 2

    val ti = tiText.toDoubleOrNull() ?: 0.0
    val v1 = v1Text.toDoubleOrNull() ?: 0.0
    val v2 = v2Text.toDoubleOrNull() ?: 0.0

    // Depth: Z1 = (ti * V1 * V2) / (2 * sqrt(V2^2 - V1^2))
    val depth = if (v2 > v1 && v1 > 0.0) {
        (ti * v1 * v2) / (2 * sqrt(v2 * v2 - v1 * v1))
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Metode Seismik Refraksi (Intercept Time Method)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tiText,
                onValueChange = { tiText = it },
                label = { Text("Intercept Time (t_i) dalam Detik") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("seismic_ti_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = v1Text,
                onValueChange = { v1Text = it },
                label = { Text("Kecepatan Lapisan Atas (V1) dalam m/s") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("seismic_v1_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = v2Text,
                onValueChange = { v2Text = it },
                label = { Text("Kecepatan Lapisan Bawah (V2) dalam m/s") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("seismic_v2_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "HASIL PERHITUNGAN:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Estimasi Kedalaman Lapisan (z1): " + String.format("%.2f", depth) + " Meter",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rumus:\nz1 = [ t_i * V1 * V2 ] / [ 2 * √(V2² - V1²) ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GeomagneticCalculator() {
    var hFieldText by remember { mutableStateOf("45320.0") }
    var hBaseText by remember { mutableStateOf("45310.0") }
    var hNormalText by remember { mutableStateOf("45000.0") }

    val hField = hFieldText.toDoubleOrNull() ?: 0.0
    val hBase = hBaseText.toDoubleOrNull() ?: 0.0
    val hNormal = hNormalText.toDoubleOrNull() ?: 0.0

    // Corrections
    val diurnalVariation = hBase - hNormal
    val hCorrected = hField - diurnalVariation

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Koreksi Harian Geomagnetik",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = hFieldText,
                onValueChange = { hFieldText = it },
                label = { Text("Pembacaan Lapangan (H_lap) dalam nT") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("geomagnetic_hlap_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hBaseText,
                onValueChange = { hBaseText = it },
                label = { Text("Pembacaan Base Station (H_base) dalam nT") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("geomagnetic_hbase_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hNormalText,
                onValueChange = { hNormalText = it },
                label = { Text("Medan Magnet Normal Regional (IGRF / H_normal) dalam nT") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("geomagnetic_hnormal_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "HASIL PERHITUNGAN:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Variasi Harian (ΔH_diurnal): " + String.format("%.1f", diurnalVariation) + " nT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intensitas Magnet Terkoreksi (H_corr): " + String.format("%.1f", hCorrected) + " nT",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rumus:\nΔH_diurnal = H_base - H_normal\nH_corr = H_lap - ΔH_diurnal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ResultDisplayField(factorK: Double, rhoA: Double, formula: String, units: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "HASIL PERHITUNGAN:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Faktor Geometri (K): " + String.format("%.3f", factorK) + " m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apparent Resistivity (ρ_a): " + String.format("%.2f", rhoA) + " $units",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rumus:\n$formula",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
