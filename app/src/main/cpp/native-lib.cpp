#include <jni.h>
#include <opencv2/core.hpp>

// Declared in edge_detection.cpp
void detectEdges(const cv::Mat& inputBgrOrRgba, cv::Mat& outputRgba);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_flamapp_NativeBridge_cannyEdges(
        JNIEnv* /* env */, jclass /* clazz */, jlong matAddrInput) {
    cv::Mat& input = *(cv::Mat*)matAddrInput; // Expect RGBA or BGR
    cv::Mat* output = new cv::Mat();
    detectEdges(input, *output);
    return reinterpret_cast<jlong>(output);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_flamapp_NativeBridge_releaseMat(
        JNIEnv* /* env */, jclass /* clazz */, jlong matAddr) {
    auto* matPtr = reinterpret_cast<cv::Mat*>(matAddr);
    if (matPtr) {
        delete matPtr;
    }
}


