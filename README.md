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

