package com.example.matrixcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("matrix_operations")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MatrixCalculatorApp()
                }
            }
        }
    }
}

@Composable
fun MatrixCalculatorApp(viewModel: MatrixViewModel = viewModel()) {
    var showResults by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),  // Make the entire screen scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Matrix Calculator",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        DimensionInputSection(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.showMatrixA) {
            MatrixInputSection(
                title = "Matrix A",
                rows = viewModel.rowsA,
                cols = viewModel.colsA,
                matrixValues = viewModel.matrixAValues,
                onValueChange = viewModel::updateMatrixA
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.showMatrixB) {
            MatrixInputSection(
                title = "Matrix B",
                rows = viewModel.rowsB,
                cols = viewModel.colsB,
                matrixValues = viewModel.matrixBValues,
                onValueChange = viewModel::updateMatrixB
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.showMatrixA && viewModel.showMatrixB) {
            OperationSelection(viewModel)
            LaunchedEffect(viewModel.resultMatrix) {
                if (viewModel.resultMatrix.isNotEmpty()) {
                    showResults = true
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showResults && viewModel.resultMatrix.isNotEmpty()) {
            MatrixResultSection(viewModel.resultMatrix)
        }
    }
}

@Composable
fun DimensionInputSection(viewModel: MatrixViewModel) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Enter Matrix Dimensions",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Matrix A: ")
            NumberInput(
                value = viewModel.rowsAInput,
                onValueChange = { viewModel.rowsAInput = it },
                label = "Rows",
                modifier = Modifier.weight(1f)
            )
            Text(" × ")
            NumberInput(
                value = viewModel.colsAInput,
                onValueChange = { viewModel.colsAInput = it },
                label = "Columns",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { viewModel.setMatrixADimensions() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Set")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Matrix B: ")
            NumberInput(
                value = viewModel.rowsBInput,
                onValueChange = { viewModel.rowsBInput = it },
                label = "Rows",
                modifier = Modifier.weight(1f)
            )
            Text(" × ")
            NumberInput(
                value = viewModel.colsBInput,
                onValueChange = { viewModel.colsBInput = it },
                label = "Columns",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { viewModel.setMatrixBDimensions() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Set")
            }
        }
    }
}

@Composable
fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            val filtered = it.filter { char -> char.isDigit() }
            onValueChange(filtered)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.padding(4.dp)
    )
}

@Composable
fun MatrixInputSection(
    title: String,
    rows: Int,
    cols: Int,
    matrixValues: List<List<String>>,
    onValueChange: (Int, Int, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            for (i in 0 until rows) {
                Row {
                    for (j in 0 until cols) {
                        OutlinedTextField(
                            value = matrixValues[i][j],
                            onValueChange = { onValueChange(i, j, it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OperationSelection(viewModel: MatrixViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Operation",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MatrixOperation.entries.forEach { operation ->
                ElevatedButton(
                    onClick = { viewModel.selectedOperation = operation },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (viewModel.selectedOperation == operation)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(60.dp),
                    enabled = viewModel.isOperationValid(operation)
                ) {
                    Text(
                        text = operation.symbol,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.calculateResult() },
            enabled = viewModel.canCalculate,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(56.dp)
                .width(200.dp)
        ) {
            Text(
                text = "Calculate",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun MatrixResultSection(resultMatrix: List<List<Double>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Result",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            resultMatrix.forEach { row ->
                Row {
                    row.forEach { value ->
                        OutlinedTextField(
                            value = String.format("%.2f", value),
                            onValueChange = { /* Read-only */ },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}