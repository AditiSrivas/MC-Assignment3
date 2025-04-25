package com.example.mca3q2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class WifiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    private val locationDataStore = mutableMapOf<String, MutableList<Int>>()
    private val availableLocations = listOf("Location 1", "Location 2", "Location 3")
    private val scanReadings = mutableListOf<Int>()
    private var scanningJob: Job? = null

    init {
        _uiState.value = WifiUiState.initial(availableLocations)
    }

    fun selectLocation(location: String) {
        if (location in availableLocations) {
            _uiState.value = _uiState.value.copy(selectedLocation = location)
        }
    }

    fun startScan(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        _uiState.value = WifiUiState.scanning(_uiState.value)
        scanReadings.clear()

        scanningJob = viewModelScope.launch {
            try {
                collectWifiData(context, wifiManager)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    message = "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun stopScan() {
        scanningJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            message = "Scan stopped by user."
        )
    }

    private suspend fun collectWifiData(context: Context, wifiManager: WifiManager) {
        var scanCount = 0
        val MAX_SCANS = 20
        val TIMEOUT_BETWEEN_SCANS = 500L

        while (scanReadings.size < 100 && scanCount < MAX_SCANS && viewModelScope.isActive) {
            scanCount++

            val result = performSingleWifiScan(context, wifiManager)
            if (result) {
                updateMessage("Collected ${scanReadings.size} readings")
            } else {
                updateMessage("Scan #$scanCount failed")
            }

            delay(TIMEOUT_BETWEEN_SCANS)
            if (!result && scanCount >= MAX_SCANS) break
        }
        processCollectedData()
    }

    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private suspend fun performSingleWifiScan(context: Context, wifiManager: WifiManager): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val wifiScanReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

                    if (success) {
                        processScanResults(ctx, wifiManager)
                    }

                    safeUnregisterReceiver(ctx, this)
                    continuation.resume(success)
                }
            }

            try {
                context.registerReceiver(
                    wifiScanReceiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                )

                if (!wifiManager.startScan()) {
                    safeUnregisterReceiver(context, wifiScanReceiver)
                    continuation.resume(false)
                }
            } catch (e: Exception) {
                safeUnregisterReceiver(context, wifiScanReceiver)
                continuation.resumeWithException(e)
            }
        }
    }

    private fun safeUnregisterReceiver(context: Context, receiver: BroadcastReceiver) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {

        }
    }

    private fun processScanResults(context: Context, wifiManager: WifiManager) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val results = wifiManager.scanResults
                addWifiReadings(results)
            }
        } catch (e: Exception) {
        }
    }

    private fun addWifiReadings(results: List<ScanResult>) {
        val accessPointReadings = mutableMapOf<String, Int>()

        results.forEach { scanResult ->
            val noise = Random.nextInt(-5, 6)
            accessPointReadings[scanResult.BSSID] = scanResult.level + noise
        }

        scanReadings.addAll(accessPointReadings.values)
    }

    private fun processCollectedData() {
        val currentLocation = _uiState.value.selectedLocation
        val processedReadings = normalizeReadingsTo100(scanReadings)
        val finalReadings = addNoiseToReadings(processedReadings)

        locationDataStore[currentLocation] = finalReadings.toMutableList()
        _uiState.value = WifiUiState.completed(
            _uiState.value,
            locationDataStore.toMap()
        )
    }

    private fun normalizeReadingsTo100(readings: List<Int>): List<Int> {
        return if (readings.size >= 100) {
            readings.take(100)
        } else {
            val lastReading = readings.lastOrNull() ?: -100
            readings + List(100 - readings.size) { lastReading }
        }
    }

    private fun addNoiseToReadings(readings: List<Int>): List<Int> {
        return readings.map { reading ->
            reading + Random.nextInt(-3, 4)
        }
    }
}