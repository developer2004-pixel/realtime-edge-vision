#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

void detectEdges(const cv::Mat& inputBgrOrRgba, cv::Mat& outputRgba) {
    cv::Mat gray;
    if (inputBgrOrRgba.channels() == 4) {
        cv::cvtColor(inputBgrOrRgba, gray, cv::COLOR_RGBA2GRAY);
    } else if (inputBgrOrRgba.channels() == 3) {
        cv::cvtColor(inputBgrOrRgba, gray, cv::COLOR_BGR2GRAY);
    } else {
        gray = inputBgrOrRgba;
    }
    cv::Mat blurred, edges;
    cv::GaussianBlur(gray, blurred, cv::Size(5, 5), 1.2);
    cv::Canny(blurred, edges, 100.0, 200.0);
    cv::cvtColor(edges, outputRgba, cv::COLOR_GRAY2RGBA);
}


