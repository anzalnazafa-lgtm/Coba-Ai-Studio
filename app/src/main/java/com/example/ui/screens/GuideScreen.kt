package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuideScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Panduan Nilai Geofisika",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Referensi cepat tabel nilai fisis material bawah permukaan (lithologi) untuk interpretasi lapangan offline.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Resistivity Reference Table
        ReferenceBlock(
            title = "1. Tahanan Jenis (Resistivitas) Batuan & Fluida",
            subtitle = "Sering digunakan untuk Geolistrik (air tanah, mineral, batuan dasar)",
            headers = listOf("Material / Batuan", "Resistivitas (Ohm.m)"),
            rows = listOf(
                listOf("Air Laut / Salin", "0.2 - 1.0"),
                listOf("Air Tanah Tawar", "10 - 100"),
                listOf("Lempung / Batulempung", "1 - 100"),
                listOf("Pasir / Batupasir", "100 - 5,000"),
                listOf("Kerikil / Kerakal Kering", "1,000 - 10,000"),
                listOf("Batugamping (Limestone)", "500 - 10,000"),
                listOf("Ansit / Basalt (Vulkanik)", "1,000 - 200,000"),
                listOf("Granit / Metamorf", "1,000 - 1,000,000")
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seismic velocity Table
        ReferenceBlock(
            title = "2. Kecepatan Gelombang P (Seismik Refraksi)",
            subtitle = "Korelasi kecepatan rambat gelombang elastis terhadap kekerasan batuan",
            headers = listOf("Medium / batuan", "Kecepatan Vp (m/s)"),
            rows = listOf(
                listOf("Udara", "330 - 340"),
                listOf("Air", "1,400 - 1,500"),
                listOf("Aluvial / Tanah Longgar", "300 - 900"),
                listOf("Pasir Jenuh Air", "1,200 - 1,800"),
                listOf("Batulempung (Silt/Clay)", "1,000 - 2,500"),
                listOf("Batupasir (Sandstone)", "2,000 - 4,500"),
                listOf("Batugamping (Limestone)", "3,000 - 6,000"),
                listOf("Basalt / Granit Tegas", "4,500 - 6,500")
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Magnetic Susceptibility Table
        ReferenceBlock(
            title = "3. Suseptibilitas Magnetik Batuan & Mineral",
            subtitle = "Kemampuan batuan magnet dalam merespons induksi medan magnet luar",
            headers = listOf("Jenis Batuan / Mineral", "Nilai (k dalam satuan SI x 10^-5)"),
            rows = listOf(
                listOf("Kuarsa (Diamagnetik)", "-1.5"),
                listOf("Batugamping / Limestone", "1 - 10"),
                listOf("Batupasir / Sandstone", "2 - 100"),
                listOf("Granit (Asam)", "10 - 2,000"),
                listOf("Gabro (Basa)", "100 - 10,000"),
                listOf("Basalt (Sangat Basa)", "500 - 20,000"),
                listOf("Magnetit / Bijih Besi", "100k - 1,000k")
            )
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ReferenceBlock(
    title: String,
    subtitle: String,
    headers: List<String>,
    rows: List<List<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Table headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp, 6.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                headers.forEachIndexed { i, header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(if (i == 0) 1.5f else 1.0f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Table rows
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(8.dp)
                ) {
                    row.forEachIndexed { i, text ->
                        Text(
                            text = text,
                            modifier = Modifier.weight(if (i == 0) 1.5f else 1.0f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
