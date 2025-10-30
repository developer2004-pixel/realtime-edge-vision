package com.flamapp

import org.opencv.android.OpenCVLoader

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
        // Initialize OpenCV when class loads
        OpenCVLoader.initDebug()
    }
    external fun cannyEdges(srcMatAddr: Long): Long
    external fun releaseMat(matAddr: Long)
}
