#include <jni.h>
#include <string>
#include <vector>
#include <stdexcept>
#include <android/log.h>

#define LOG_TAG "MatrixOperations"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using Matrix = std::vector<std::vector<double>>;

Matrix javaListToMatrix(JNIEnv *env, jobject javaList) {
    jclass listClass = env->FindClass("java/util/List");
    jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

    jint outerSize = env->CallIntMethod(javaList, sizeMethod);
    Matrix matrix(outerSize);

    for (int i = 0; i < outerSize; i++) {
        jobject innerList = env->CallObjectMethod(javaList, getMethod, i);
        jint innerSize = env->CallIntMethod(innerList, sizeMethod);

        matrix[i].resize(innerSize);

        for (int j = 0; j < innerSize; j++) {
            jobject doubleObj = env->CallObjectMethod(innerList, getMethod, j);
            jclass doubleClass = env->FindClass("java/lang/Double");
            jmethodID doubleValueMethod = env->GetMethodID(doubleClass, "doubleValue", "()D");

            jdouble value = env->CallDoubleMethod(doubleObj, doubleValueMethod);
            matrix[i][j] = value;
            env->DeleteLocalRef(doubleObj);
        }
        env->DeleteLocalRef(innerList);
    }
    return matrix;
}

jobject matrixToJavaList(JNIEnv *env, const Matrix& matrix) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");

    jobject outerList = env->NewObject(arrayListClass, arrayListConstructor);

    for (const auto& row : matrix) {
        jobject innerList = env->NewObject(arrayListClass, arrayListConstructor);

        for (double value : row) {
            jclass doubleClass = env->FindClass("java/lang/Double");
            jmethodID doubleConstructor = env->GetMethodID(doubleClass, "<init>", "(D)V");
            jobject doubleObj = env->NewObject(doubleClass, doubleConstructor, value);

            env->CallBooleanMethod(innerList, addMethod, doubleObj);
            env->DeleteLocalRef(doubleObj);
        }

        env->CallBooleanMethod(outerList, addMethod, innerList);
        env->DeleteLocalRef(innerList);
    }

    return outerList;
}

// Matrix addition
Matrix addMatrices(const Matrix& a, const Matrix& b) {
    if (a.size() != b.size() || a[0].size() != b[0].size()) {
        throw std::invalid_argument("Matrix dimensions must match for addition");
    }
    Matrix result(a.size(), std::vector<double>(a[0].size()));

    for (size_t i = 0; i < a.size(); i++) {
        for (size_t j = 0; j < a[0].size(); j++) {
            result[i][j] = a[i][j] + b[i][j];
        }
    }
    return result;
}

// Matrix subtraction
Matrix subtractMatrices(const Matrix& a, const Matrix& b) {
    if (a.size() != b.size() || a[0].size() != b[0].size()) {
        throw std::invalid_argument("Matrix dimensions must match for subtraction");
    }
    Matrix result(a.size(), std::vector<double>(a[0].size()));

    for (size_t i = 0; i < a.size(); i++) {
        for (size_t j = 0; j < a[0].size(); j++) {
            result[i][j] = a[i][j] - b[i][j];
        }
    }
    return result;
}

// Matrix multiplication
Matrix multiplyMatrices(const Matrix& a, const Matrix& b) {
    if (a[0].size() != b.size()) {
        throw std::invalid_argument("Matrix dimensions incompatible for multiplication");
    }
    Matrix result(a.size(), std::vector<double>(b[0].size(), 0));

    for (size_t i = 0; i < a.size(); i++) {
        for (size_t j = 0; j < b[0].size(); j++) {
            for (size_t k = 0; k < b.size(); k++) {
                result[i][j] += a[i][k] * b[k][j];
            }
        }
    }
    return result;
}

double determinant(const Matrix& matrix) {
    size_t n = matrix.size();

    if (n == 1) return matrix[0][0];
    else if (n == 2) {
        return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
    }
    else if (n == 3) {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
               - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
               + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }
    else {
        double det = 0;
        for (size_t j = 0; j < n; j++) {
            Matrix minor(n - 1, std::vector<double>(n - 1));

            for (size_t i = 1; i < n; i++) {
                size_t col_index = 0;
                for (size_t k = 0; k < n; k++) {
                    if (k == j) continue;
                    minor[i-1][col_index++] = matrix[i][k];
                }
            }

            det += ((j % 2 == 0) ? 1 : -1) * matrix[0][j] * determinant(minor);
        }
        return det;
    }
}

Matrix invertMatrix(const Matrix& matrix) {
    size_t n = matrix.size();

    if (n != matrix[0].size()) {
        throw std::invalid_argument("Matrix must be square for inversion");
    }
    double det = determinant(matrix);

    if (std::abs(det) < 1e-10) {
        throw std::invalid_argument("Matrix is singular and cannot be inverted");
    }

    if (n == 1) {
        return {{1.0 / matrix[0][0]}};
    }
    else if (n == 2) {
        Matrix result(2, std::vector<double>(2));
        result[0][0] = matrix[1][1] / det;
        result[0][1] = -matrix[0][1] / det;
        result[1][0] = -matrix[1][0] / det;
        result[1][1] = matrix[0][0] / det;
        return result;
    }
    else {
        Matrix cofactors(n, std::vector<double>(n));
        for (size_t i = 0; i < n; i++) {
            for (size_t j = 0; j < n; j++) {
                Matrix minor(n - 1, std::vector<double>(n - 1));

                for (size_t r = 0, r_minor = 0; r < n; r++) {
                    if (r == i) continue;
                    for (size_t c = 0, c_minor = 0; c < n; c++) {
                        if (c == j) continue;
                        minor[r_minor][c_minor++] = matrix[r][c];
                    }
                    r_minor++;
                }

                double cofactor = ((i + j) % 2 == 0 ? 1 : -1) * determinant(minor);
                cofactors[j][i] = cofactor;
            }
        }

        for (size_t i = 0; i < n; i++) {
            for (size_t j = 0; j < n; j++) {
                cofactors[i][j] /= det;
            }
        }

        return cofactors;
    }
}

// Matrix division
Matrix divideMatrices(const Matrix& a, const Matrix& b) {
    if (b.size() != b[0].size()) {
        throw std::invalid_argument("Second matrix must be square for division");
    }

    Matrix b_inverse = invertMatrix(b);
    return multiplyMatrices(a, b_inverse);
}

extern "C" {
JNIEXPORT jobject JNICALL
Java_com_example_matrixcalculator_MatrixViewModel_addMatrices(
        JNIEnv *env, jobject thiz, jobject matrix_a, jobject matrix_b) {
    try {
        Matrix a = javaListToMatrix(env, matrix_a);
        Matrix b = javaListToMatrix(env, matrix_b);
        Matrix result = addMatrices(a, b);
        return matrixToJavaList(env, result);
    } catch (const std::exception& e) {
        LOGE("Error in addMatrices: %s", e.what());
        return matrixToJavaList(env, Matrix()); // Return empty matrix on error
    }
}

JNIEXPORT jobject JNICALL
Java_com_example_matrixcalculator_MatrixViewModel_subtractMatrices(
        JNIEnv *env, jobject thiz, jobject matrix_a, jobject matrix_b) {
    try {
        Matrix a = javaListToMatrix(env, matrix_a);
        Matrix b = javaListToMatrix(env, matrix_b);
        Matrix result = subtractMatrices(a, b);
        return matrixToJavaList(env, result);
    } catch (const std::exception& e) {
        LOGE("Error in subtractMatrices: %s", e.what());
        return matrixToJavaList(env, Matrix());
    }
}

JNIEXPORT jobject JNICALL
Java_com_example_matrixcalculator_MatrixViewModel_multiplyMatrices(
        JNIEnv *env, jobject thiz, jobject matrix_a, jobject matrix_b) {
    try {
        Matrix a = javaListToMatrix(env, matrix_a);
        Matrix b = javaListToMatrix(env, matrix_b);
        Matrix result = multiplyMatrices(a, b);
        return matrixToJavaList(env, result);
    } catch (const std::exception& e) {
        LOGE("Error in multiplyMatrices: %s", e.what());
        return matrixToJavaList(env, Matrix());
    }
}

JNIEXPORT jobject JNICALL
Java_com_example_matrixcalculator_MatrixViewModel_divideMatrices(
        JNIEnv *env, jobject thiz, jobject matrix_a, jobject matrix_b) {
    try {
        Matrix a = javaListToMatrix(env, matrix_a);
        Matrix b = javaListToMatrix(env, matrix_b);
        Matrix result = divideMatrices(a, b);
        return matrixToJavaList(env, result);
    } catch (const std::exception& e) {
        LOGE("Error in divideMatrices: %s", e.what());
        return matrixToJavaList(env, Matrix());
    }
}
}