package com.example.matrixcalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class MatrixOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("×"),
    DIVIDE("÷")
}

class MatrixViewModel : ViewModel() {
    // Matrix A
    var rowsA by mutableIntStateOf(2)
    var colsA by mutableIntStateOf(2)
    var rowsAInput by mutableStateOf("")
    var colsAInput by mutableStateOf("")
    var showMatrixA by mutableStateOf(false)
    private var matrixA = mutableListOf<MutableList<Double>>()
    var matrixAValues by mutableStateOf(List(2) { List(2) { "" } })

    // Matrix B
    var rowsB by mutableIntStateOf(2)
    var colsB by mutableIntStateOf(2)
    var rowsBInput by mutableStateOf("")
    var colsBInput by mutableStateOf("")
    var showMatrixB by mutableStateOf(false)
    private var matrixB = mutableListOf<MutableList<Double>>()
    var matrixBValues by mutableStateOf(List(2) { List(2) { "" } })

    var selectedOperation by mutableStateOf(MatrixOperation.ADD)
    var resultMatrix by mutableStateOf<List<List<Double>>>(emptyList())

    val canCalculate: Boolean
        get() {
            val allAInputsValid = matrixAValues.all { row -> row.all { it.isNotBlank() && it.toDoubleOrNull() != null } }
            val allBInputsValid = matrixBValues.all { row -> row.all { it.isNotBlank() && it.toDoubleOrNull() != null } }

            return showMatrixA && showMatrixB && allAInputsValid && allBInputsValid && isOperationValid(selectedOperation)
        }

    init {
        initializeMatrix(matrixA, rowsA, colsA)
        initializeMatrix(matrixB, rowsB, colsB)
    }

    private fun initializeMatrix(matrix: MutableList<MutableList<Double>>, rows: Int, cols: Int) {
        matrix.clear()
        for (i in 0 until rows) {
            val row = MutableList(cols) { 0.0 }
            matrix.add(row)
        }
    }

    fun setMatrixADimensions() {
        val rows = rowsAInput.toIntOrNull() ?: 2
        val cols = colsAInput.toIntOrNull() ?: 2

        if (rows > 0 && cols > 0) {
            rowsA = rows
            colsA = cols
            showMatrixA = true

            matrixA = mutableListOf()
            initializeMatrix(matrixA, rowsA, colsA)
            matrixAValues = List(rowsA) { List(colsA) { "" } }
        }
    }

    fun setMatrixBDimensions() {
        val rows = rowsBInput.toIntOrNull() ?: 2
        val cols = colsBInput.toIntOrNull() ?: 2

        if (rows > 0 && cols > 0) {
            rowsB = rows
            colsB = cols
            showMatrixB = true
            matrixB = mutableListOf()
            initializeMatrix(matrixB, rowsB, colsB)

            matrixBValues = List(rowsB) { List(colsB) { "" } }
        }
    }

    fun updateMatrixA(row: Int, col: Int, value: String) {
        if (row < rowsA && col < colsA) {
            val newMatrixValues = matrixAValues.toMutableList()
            val newRow = if (row < matrixAValues.size) {
                matrixAValues[row].toMutableList()
            } else {
                MutableList(colsA) { "" }
            }

            if (col < newRow.size) {
                newRow[col] = value
            }

            if (row < newMatrixValues.size) {
                newMatrixValues[row] = newRow
            }

            matrixAValues = newMatrixValues

            if (row < matrixA.size && col < matrixA[row].size) {
                matrixA[row][col] = value.toDoubleOrNull() ?: 0.0
            }
        }
    }

    fun updateMatrixB(row: Int, col: Int, value: String) {
        if (row < rowsB && col < colsB) {
            val newMatrixValues = matrixBValues.toMutableList()
            val newRow = if (row < matrixBValues.size) {
                matrixBValues[row].toMutableList()
            } else {
                MutableList(colsB) { "" }
            }

            if (col < newRow.size) {
                newRow[col] = value
            }

            if (row < newMatrixValues.size) {
                newMatrixValues[row] = newRow
            }

            matrixBValues = newMatrixValues

            if (row < matrixB.size && col < matrixB[row].size) {
                matrixB[row][col] = value.toDoubleOrNull() ?: 0.0
            }
        }
    }

    fun isOperationValid(operation: MatrixOperation): Boolean {
        return when (operation) {
            MatrixOperation.ADD, MatrixOperation.SUBTRACT -> rowsA == rowsB && colsA == colsB
            MatrixOperation.MULTIPLY -> colsA == rowsB
            MatrixOperation.DIVIDE -> rowsB == colsB && colsB > 0 && isInvertible(matrixB)
        }
    }

    private fun isInvertible(matrix: List<MutableList<Double>>): Boolean {
        if (matrix.isEmpty() || matrix.size != matrix[0].size) {
            return false
        }

        return matrix.size == matrix[0].size
    }

    fun calculateResult() {
        try {
            if (matrixA.size != rowsA || matrixA.any { it.size != colsA } ||
                matrixB.size != rowsB || matrixB.any { it.size != colsB }) {

                synchronizeMatricesFromUI()
            }

            resultMatrix = when (selectedOperation) {
                MatrixOperation.ADD -> addMatrices(matrixA, matrixB)
                MatrixOperation.SUBTRACT -> subtractMatrices(matrixA, matrixB)
                MatrixOperation.MULTIPLY -> multiplyMatrices(matrixA, matrixB)
                MatrixOperation.DIVIDE -> divideMatrices(matrixA, matrixB)
            }
        } catch (e: Exception) {
            resultMatrix = emptyList()
        }
    }

    private fun synchronizeMatricesFromUI() {
        matrixA = mutableListOf()
        matrixB = mutableListOf()

        initializeMatrix(matrixA, rowsA, colsA)
        initializeMatrix(matrixB, rowsB, colsB)

        for (i in 0 until rowsA) {
            for (j in 0 until colsA) {
                if (i < matrixAValues.size && j < matrixAValues[i].size) {
                    matrixA[i][j] = matrixAValues[i][j].toDoubleOrNull() ?: 0.0
                }
            }
        }

        for (i in 0 until rowsB) {
            for (j in 0 until colsB) {
                if (i < matrixBValues.size && j < matrixBValues[i].size) {
                    matrixB[i][j] = matrixBValues[i][j].toDoubleOrNull() ?: 0.0
                }
            }
        }
    }

    private external fun addMatrices(matrixA: List<List<Double>>, matrixB: List<List<Double>>): List<List<Double>>
    private external fun subtractMatrices(matrixA: List<List<Double>>, matrixB: List<List<Double>>): List<List<Double>>
    private external fun multiplyMatrices(matrixA: List<List<Double>>, matrixB: List<List<Double>>): List<List<Double>>
    private external fun divideMatrices(matrixA: List<List<Double>>, matrixB: List<List<Double>>): List<List<Double>>
}