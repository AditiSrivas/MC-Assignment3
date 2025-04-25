cmake_minimum_required(VERSION 3.10.2)
project("matrix calculator")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
        matrix_operations
        SHARED
        matrix_operations.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        matrix_operations
        ${log-lib}
)