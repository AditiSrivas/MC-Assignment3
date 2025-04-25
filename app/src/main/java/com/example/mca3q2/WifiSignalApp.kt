package com.example.mca3q2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mca3q2.ui.theme.MCA3Q2Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiSignalApp(
    viewModel: WifiViewModel,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        AppContent(
            uiState = uiState,
            onLocationSelected = { viewModel.selectLocation(it) },
            onStartScan = onStartScan,
            onStopScan = onStopScan,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppContent(
    uiState: WifiUiState,
    onLocationSelected: (String) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppHeader()
        LocationSelector(
            locations = uiState.locations,
            selectedLocation = uiState.selectedLocation,
            onLocationSelected = onLocationSelected
        )

        ScanControlButtons(
            isScanning = uiState.isScanning,
            onStartScan = onStartScan,
            onStopScan = onStopScan
        )

        if (uiState.message.isNotEmpty()) {
            StatusMessage(message = uiState.message)
        }

        if (uiState.locationData.isNotEmpty()) {
            WifiDataDisplay(
                locationData = uiState.locationData,
                selectedLocation = uiState.selectedLocation
            )
        }
    }
}

@Composable
private fun AppHeader() {
    Text(
        text = "WiFi Signal Strength Logger",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelector(
    locations: List<String>,
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedLocation,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location) },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScanControlButtons(
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onStartScan,
            enabled = !isScanning,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            Text("Start Scan")
        }

        Button(
            onClick = onStopScan,
            enabled = isScanning,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Stop Scan")
        }
    }
}

@Composable
private fun StatusMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun WifiDataDisplay(
    locationData: Map<String, List<Int>>,
    selectedLocation: String
) {
    Text(
        text = "WiFi Signal Data",
        style = MaterialTheme.typography.titleLarge
    )

    locationData[selectedLocation]?.let { data ->
        val statistics = calculateSignalStatistics(data)

        Text(
            text = "$selectedLocation (Avg: ${statistics.average} dBm, Range: ${statistics.min} to ${statistics.max} dBm)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SignalStrengthGrid(signalData = data)
    }
}

private data class SignalStatistics(val average: Int, val min: Int, val max: Int)
private fun calculateSignalStatistics(data: List<Int>): SignalStatistics {
    val nonZeroData = data.filter { it != 0 }

    val avg = nonZeroData.average().let {
        if (it.isNaN()) 0.0 else it
    }.toInt()

    val min = nonZeroData.minOrNull() ?: 0
    val max = nonZeroData.maxOrNull() ?: 0

    return SignalStatistics(avg, min, max)
}

@Composable
private fun SignalStrengthGrid(signalData: List<Int>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
    ) {
        items(signalData) { rssi ->
            SignalStrengthCell(rssi = rssi)
        }
    }
}

@Composable
private fun SignalStrengthCell(rssi: Int) {
    val color = getColorForSignalStrength(rssi)

    Box(
        modifier = Modifier
            .size(32.dp)
            .padding(1.dp)
            .background(color.copy(alpha = 0.3f))
            .border(1.dp, color),
        contentAlignment = Alignment.Center
    ) {
        if (rssi != 0) {
            Text(
                text = rssi.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getColorForSignalStrength(rssi: Int): Color {
    return when {
        rssi > -50 -> Color.Green
        rssi > -70 -> Color.Yellow
        rssi != 0 -> Color.Red
        else -> Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWifiSignalApp() {
    MCA3Q2Theme {
        WifiSignalApp(WifiViewModel(), {}, {})
    }
}