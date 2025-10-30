cmake_minimum_required(VERSION 3.22.1)
project(flamapp)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV package
find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})

add_library(
    native-lib
    SHARED
    native-lib.cpp
    edge_detection.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
    native-lib
    ${OpenCV_LIBS}
    ${log-lib}
    ${android-lib}
    ${jnigraphics-lib}
)


