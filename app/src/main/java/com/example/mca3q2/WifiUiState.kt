package com.example.mca3q2

data class WifiUiState(
    val isScanning: Boolean = false,
    val selectedLocation: String = "",
    val locations: List<String> = emptyList(),
    val locationData: Map<String, List<Int>> = emptyMap(),
    val message: String = ""
) {
    companion object {
        fun initial(locations: List<String>): WifiUiState {
            return WifiUiState(
                locations = locations,
                selectedLocation = locations.firstOrNull().orEmpty()
            )
        }

        fun scanning(currentState: WifiUiState): WifiUiState {
            return currentState.copy(
                isScanning = true,
                message = "Starting scan..."
            )
        }

        fun completed(currentState: WifiUiState, locationData: Map<String, List<Int>>): WifiUiState {
            return currentState.copy(
                isScanning = false,
                locationData = locationData,
                message = "Completed: 100 readings collected for ${currentState.selectedLocation}"
            )
        }
    }
}