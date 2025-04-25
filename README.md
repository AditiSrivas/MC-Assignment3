# MC-Assignment3
## Question 1: Matrix Calculator

### Overview
Matrix Calculator is an Android application that allows users to perform various matrix operations like addition, subtraction, multiplication, and division. The application is built using Jetpack Compose for the UI and leverages native C/C++ code through the JNI (Java Native Interface) for efficient matrix operations.
Features

Create matrices of custom dimensions
Support for basic matrix operations:
* Addition
* Subtraction
* Multiplication
* Division (matrix inverse)

Real-time validation of operations based on matrix dimensions
* Clean and intuitive UI built with Jetpack Compose
* Efficient calculations using native code

### Technical Architecture
The application follows a Model-View-ViewModel (MVVM) architecture:
* UI Layer: Implemented using Jetpack Compose with composable functions for different UI components
* ViewModel: Manages UI state and business logic
* Native Layer: Handles complex matrix operations in C/C++ for optimal performance

### Implementation Details
#### UI Components
* MatrixCalculatorApp: Main composable that orchestrates the entire UI
* DimensionInputSection: Input fields for matrix dimensions
* MatrixInputSection: Dynamic grid for entering matrix values
* OperationSelection: Buttons for selecting matrix operations
* MatrixResultSection: Display area for operation results

#### Core Classes
* MatrixViewModel: Manages application state and matrix operations
* MatrixOperation: Enum for supported operations (ADD, SUBTRACT, MULTIPLY, DIVIDE)

### Native Integration
The app uses JNI to interact with native C/C++ code for matrix operations:
* System.loadLibrary("matrix_operations") loads the native library
* Native functions for matrix operations are declared with the external keyword

### Requirements
Android Studio Hedgehog (2023.1.1) or newer
Android SDK 24 or higher
NDK for native code compilation
Kotlin 1.9.0 or newer
Jetpack Compose 1.5.0 or newer

### Setup Instructions
Clone the repository
Open the project in Android Studio
Sync Gradle files
Build the native code components by running the ndk-build task
Run the application on an emulator or physical device

### Native Code
The application performs matrix operations in native code. The JNI interface includes:
* addMatrices: Adds two matrices of the same dimensions
* subtractMatrices: Subtracts one matrix from another
* multiplyMatrices: Multiplies two matrices (A×B)
* divideMatrices: Performs matrix division (A×B⁻¹)

### Future Enhancements
Support for more matrix operations (determinant, transpose, etc.)
Matrix presets and templates
Step-by-step solution display
Matrix visualization tools
Export/import capabilities

## Question 2: WiFi Signal Strength Logger

### Overview
WiFi Signal Logger is an Android application for collecting and visualizing WiFi signal strength data at different locations. This app allows users to gather signal strength readings (RSSI values) from nearby access points and visualize them in a grid format, helping to analyze WiFi coverage across different physical spaces.
Features

* Location-based WiFi signal strength logging
* Real-time scanning and data collection
* Visual representation of signal strength using color-coded grid
* Statistical analysis of signal strength data (average, range)
* Multiple location support for comparative analysis
* Automatic noise simulation for realistic readings

### Technical Details
#### Architecture
The application is built using modern Android development practices:
* MVVM Architecture: Clear separation of UI, business logic, and data
* Jetpack Compose: Declarative UI toolkit for modern Android UI
* Kotlin Coroutines: For asynchronous operations and reactive programming
* StateFlow: For reactive UI state management

### Key Components
* WifiViewModel: Manages WiFi scanning operations and UI state
* WifiUiState: Data class for representing application state
* WifiSignalApp: Main composable UI component
* BroadcastReceiver: For handling WiFi scan results

### Permissions Required
The application requires the following permissions:
* ACCESS_FINE_LOCATION: Required for accessing detailed WiFi scan results
* ACCESS_WIFI_STATE: For reading WiFi state information
* CHANGE_WIFI_STATE: For initiating WiFi scans

### Getting Started

#### Prerequisites
* Android Studio Arctic Fox (2020.3.1) or newer
* Android SDK 21+
* Kotlin 1.5.0+
* A device with WiFi capabilities

#### Building the Project
* Clone the repository
* Open the project in Android Studio
* Sync the project with Gradle files
* Build and run the application on a device or emulator

### Usage Instructions
Launch the application
Select a location from the dropdown menu
Press "Start Scan" to begin collecting WiFi signal data
View collected data in the grid display
Repeat for different locations to compare signal strength

### Understanding the Data
The app displays WiFi signal strength in dBm (decibels relative to a milliwatt):
* Green cells (-50 dBm or higher): Strong signal
* Yellow cells (-70 to -50 dBm): Moderate signal
* Red cells (below -70 dBm): Weak signal
* Gray cells (0 dBm): No signal

### Implementation Notes
The app collects up to 100 readings from available access points
Readings are normalized and noise is added to simulate real-world conditions
If insufficient readings are available, the last reading is duplicated
Scanning continues until 100 readings are collected or the maximum scan count is reached

### Future Enhancements
Heatmap visualization of signal strength across floor plans
Export data functionality for further analysis
Automated location detection
Signal strength prediction for unmeasured areas
Support for 5GHz networks analysis
