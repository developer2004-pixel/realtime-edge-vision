#ifndef FLAMAPP_EDGE_DETECTION_H
#define FLAMAPP_EDGE_DETECTION_H

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

void detectEdges(const cv::Mat& inputBgrOrRgba, cv::Mat& outputRgba);

#endif // FLAMAPP_EDGE_DETECTION_H
