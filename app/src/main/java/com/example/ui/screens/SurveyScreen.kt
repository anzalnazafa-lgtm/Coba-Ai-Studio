package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurveyLog
import com.example.data.model.SurveyReading
import com.example.ui.components.VisualProfileChart
import com.example.ui.viewmodel.SurveyViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.random.Random

@Composable
fun SurveyScreen(viewModel: SurveyViewModel) {
    val logs by viewModel.allLogs.collectAsState()
    val selectedLog by viewModel.selectedLog.collectAsState()
    val readings by viewModel.selectedLogReadings.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = selectedLog,
        transitionSpec = {
            slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
        },
        label = "SurveyNavigation"
    ) { activeLog ->
        if (activeLog == null) {
            // LIST VIEW
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sistem Log Survei Lapangan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Catat dan proses data stasiun pengukuran lapangan langsung secara digital.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Empty",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada log survei.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tekan tombol + di bawah untuk membuat log baru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logs) { log ->
                                SurveyLogCard(
                                    log = log,
                                    onClick = { viewModel.selectLog(log) },
                                    onDelete = { viewModel.deleteLog(log) }
                                )
                            }
                        }
                    }
                }

                // FAB floating action button
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .testTag("create_log_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Log Baru")
                }
            }
        } else {
            // DETAIL VIEW (READINGS FOR SELECTED LOG)
            LogDetailPage(
                log = activeLog,
                readings = readings,
                onBack = { viewModel.selectLog(null) },
                onAddReading = { name, elev, lat, lon, v1, v2, appVal, notes ->
                    viewModel.addReading(name, elev, lat, lon, v1, v2, appVal, notes)
                },
                onDeleteReading = { viewModel.deleteReading(it) },
                onClearAll = { viewModel.clearReadings(activeLog.id) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showCreateDialog) {
        CreateLogDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, operator, method, notes, sp1, sp2 ->
                viewModel.createLog(name, operator, method, notes, sp1, sp2)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun SurveyLogCard(
    log: SurveyLog,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("survey_log_item_${log.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = "Method",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.method,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Operator: ${log.operator}  •  ${log.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Log",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, operator: String, method: String, notes: String, sp1: Double, sp2: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val methods = listOf("Resistivity (Wenner)", "Resistivity (Schlumberger)", "Geomagnetic")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var expandedMethodDropdown by remember { mutableStateOf(false) }

    // Spacing initial parameters defaults
    var sp1Text by remember { mutableStateOf("2.0") } // spacing 'a' for wenner or default AB/2
    var sp2Text by remember { mutableStateOf("1.0") } // default MN/2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mulai Log Survei Baru") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Proyek / Lintasan") },
                    placeholder = { Text("Contoh: Lintasan Geolistrik A") },
                    modifier = Modifier.fillMaxWidth().testTag("add_log_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = operator,
                    onValueChange = { operator = it },
                    label = { Text("Petugas Lapangan (Operator)") },
                    placeholder = { Text("Nama Anda") },
                    modifier = Modifier.fillMaxWidth().testTag("add_log_operator"),
                    singleLine = true
                )

                // Micro Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedMethodDropdown,
                    onExpandedChange = { expandedMethodDropdown = !expandedMethodDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Metode Akuisisi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethodDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("add_log_method_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMethodDropdown,
                        onDismissRequest = { expandedMethodDropdown = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethod = method
                                    expandedMethodDropdown = false
                                    // Adjust setup spacer presets
                                    if (method == "Geomagnetic") {
                                        sp1Text = "0.0"
                                        sp2Text = "0.0"
                                    } else if (method == "Resistivity (Wenner)") {
                                        sp1Text = "2.0"
                                        sp2Text = "0.0"
                                    } else {
                                        sp1Text = "10.0"
                                        sp2Text = "1.0"
                                    }
                                }
                            )
                        }
                    }
                }

                // Conditionally show initial spacers
                if (selectedMethod == "Resistivity (Wenner)") {
                    OutlinedTextField(
                        value = sp1Text,
                        onValueChange = { sp1Text = it },
                        label = { Text("Spasi Elektroda (a) Aktual Meter") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else if (selectedMethod == "Resistivity (Schlumberger)") {
                    OutlinedTextField(
                        value = sp1Text,
                        onValueChange = { sp1Text = it },
                        label = { Text("Spasi Arus Awal (AB/2) Meter") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sp2Text,
                        onValueChange = { sp2Text = it },
                        label = { Text("Spasi Potensial Awal (MN/2) Meter") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Cuaca / Lokasi") },
                    placeholder = { Text("Contoh: Cuaca cerah, formasi batugamping") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && operator.isNotEmpty()) {
                        val simpleDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                        val sp1 = sp1Text.toDoubleOrNull() ?: 1.0
                        val sp2 = sp2Text.toDoubleOrNull() ?: 1.0
                        onConfirm(name, operator, selectedMethod, notes, sp1, sp2)
                    }
                },
                enabled = name.isNotEmpty() && operator.isNotEmpty()
            ) {
                Text("Buat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun LogDetailPage(
    log: SurveyLog,
    readings: List<SurveyReading>,
    onBack: () -> Unit,
    onAddReading: (
        name: String, elev: Double, lat: Double, lon: Double,
        v1: Double, v2: Double, appVal: Double, notes: String
    ) -> Unit,
    onDeleteReading: (SurveyReading) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Add reading controls states
    var stationName by remember { mutableStateOf("S-${readings.size + 1}") }
    var val1Text by remember { mutableStateOf("") } // resistance (Ohm) or geomagnetic signal (nT)
    var val2Text by remember { mutableStateOf("") } // secondary space or diurnal drift (nT)
    
    // Auto-population setup based on log methods
    LaunchedEffect(readings.size) {
        stationName = "S-${readings.size + 1}"
    }

    // Coordinates helpers
    var elevation by remember { mutableStateOf("0.0") }
    var latitude by remember { mutableStateOf("0.0") }
    var longitude by remember { mutableStateOf("0.0") }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = log.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${log.method} • Operator: ${log.operator}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Profile Canvas Chart (Apparent Value vs Stations)
        val chartYLabel = if (log.method.contains("Resistivity")) "ρ_a (Ohm.m)" else "H_corr (nT)"
        VisualProfileChart(
            readings = readings,
            valueSelector = { it.apparentValue },
            yLabel = chartYLabel,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Form to add a new reading station
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Input data Stasiun / Titik No. ${readings.size + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stationName,
                        onValueChange = { stationName = it },
                        label = { Text("Stasiun") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Helper to random simulate coordinates
                    Button(
                        onClick = {
                            elevation = String.format("%.1f", Random.nextDouble(50.0, 350.0))
                            latitude = String.format("%.6f", Random.nextDouble(-8.5, -6.0))
                            longitude = String.format("%.6f", Random.nextDouble(106.5, 114.0))
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Sim")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simul Koordinat", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Lintang (Lat)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Bujur (Lon)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Method specific value fields
                if (log.method == "Resistivity (Wenner)") {
                    val1Text = log.spacingParam1.toString() // static spacer 'a'
                    OutlinedTextField(
                        value = val2Text, // Let user insert R Ohm
                        onValueChange = { val2Text = it },
                        label = { Text("Resistansi R (Ohm)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_val_resistance"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                } else if (log.method == "Resistivity (Schlumberger)") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = val1Text, // AB/2
                            onValueChange = { val1Text = it },
                            label = { Text("Spasi AB/2 (m)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = val2Text, // R (Ohm)
                            onValueChange = { val2Text = it },
                            label = { Text("Resistansi R (Ohm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                } else {
                    // Geomagnetic
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = val1Text, // H_0
                            onValueChange = { val1Text = it },
                            label = { Text("Banyak Data H_lap (nT)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = val2Text, // diurnal drift dH
                            onValueChange = { val2Text = it },
                            label = { Text("Variasi Harian (nT)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val elev = elevation.toDoubleOrNull() ?: 0.0
                        val lat = latitude.toDoubleOrNull() ?: 0.0
                        val lon = longitude.toDoubleOrNull() ?: 0.0

                        var v1 = 0.0
                        var v2 = 0.0
                        var appVal = 0.0

                        if (log.method == "Resistivity (Wenner)") {
                            // value1 = a, value2 = R
                            v1 = log.spacingParam1
                            v2 = val2Text.toDoubleOrNull() ?: 0.0
                            val k = 2 * PI * v1
                            appVal = k * v2
                        } else if (log.method == "Resistivity (Schlumberger)") {
                            // value1 = AB/2, value2 = R, log's param2 = MN/2
                            v1 = val1Text.toDoubleOrNull() ?: log.spacingParam1
                            v2 = val2Text.toDoubleOrNull() ?: 0.0
                            val mn2 = log.spacingParam2
                            val k = if (mn2 > 0) (PI * (v1 * v1 - mn2 * mn2)) / (2 * mn2) else 1.0
                            appVal = k * v2
                        } else {
                            // Geomagnetic: value1 = H_lap, value2 = dH diurnal
                            v1 = val1Text.toDoubleOrNull() ?: 0.0
                            v2 = val2Text.toDoubleOrNull() ?: 0.0
                            appVal = v1 - v2
                        }

                        onAddReading(stationName, elev, lat, lon, v1, v2, appVal, "")
                        
                        // Clear input texts for next cycle
                        if (log.method == "Resistivity (Schlumberger)") {
                            val2Text = ""
                        } else {
                            val2Text = ""
                            val1Text = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_reading_button"),
                    enabled = stationName.isNotEmpty() && val2Text.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hitung & Simpan Stasiun")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Readings table list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tabel Data Lapangan (${readings.size} Titik)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onClearAll, enabled = readings.isNotEmpty()) {
                Text("Bersihkan Semua", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (readings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada stasiun terdaftar. Tambahkan di atas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            readings.forEachIndexed { i, r ->
                val bg = if (i % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stasiun: ${r.stationName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val detailsText = if (log.method.contains("Resistivity")) {
                            "R: ${r.value2} Ohm  •  ρ_a: " + String.format("%.2f", r.apparentValue) + " Ohm.m"
                        } else {
                            "H_lap: ${r.value1} nT  •  H_corr: " + String.format("%.1f", r.apparentValue) + " nT"
                        }
                        Text(
                            text = detailsText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lat: ${r.latitude}, Lon: ${r.longitude}  •  Elev: ${r.elevation}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onDeleteReading(r) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Stasiun",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
