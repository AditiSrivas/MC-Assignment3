package com.example.mca3q2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.mca3q2.ui.theme.MCA3Q2Theme

class MainActivity : ComponentActivity() {
    private val viewModel: WifiViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.updateMessage("Permissions granted, ready to scan")
        } else {
            viewModel.updateMessage("Required permissions not granted. WiFi scanning may not work.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            MCA3Q2Theme {
                WifiSignalApp(
                    viewModel = viewModel,
                    onStartScan = { initiateWifiScan() },
                    onStopScan = { viewModel.stopScan() }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions)
        }
    }

    private fun initiateWifiScan() {
        viewModel.startScan(this)
    }
}