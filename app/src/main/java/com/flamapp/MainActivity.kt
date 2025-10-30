package com.flamapp

import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flamapp.camera.CameraManager
import com.flamapp.gl.TextureRenderer
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: TextureRenderer
    private lateinit var cameraManager: CameraManager
    
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request camera permission
        if (!CameraManager.checkPermission(this)) {
            CameraManager.requestPermission(this)
            return
        }
        
        setupViews()
        startCamera()
    }
    
    private fun setupViews() {
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            renderer = TextureRenderer()
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        setContentView(glSurfaceView)
        
        Log.d(TAG, "Views initialized")
    }
    
    private fun startCamera() {
        cameraManager = CameraManager(this).apply {
            onFrameAvailable = { imageReader ->
                val image = imageReader.acquireLatestImage()
                image?.let {
                    try {
                        // Convert YUV_420_888 to RGBA Mat
                        val yuvMat = imageToMat(it)
                        val rgbaMat = Mat()
                        org.opencv.imgproc.Imgproc.cvtColor(yuvMat, rgbaMat, org.opencv.imgproc.Imgproc.COLOR_YUV2RGBA_NV21)
                        
                        // Process with native Canny edge detection
                        val processedMatAddr = NativeBridge.cannyEdges(rgbaMat.nativeObjAddr)
                        val processedMat = Mat(processedMatAddr)
                        
                        // Convert to ByteBuffer for OpenGL
                        val buffer = ByteBuffer.allocateDirect(processedMat.rows() * processedMat.cols() * 4)
                        processedMat.get(0, 0, buffer.array())
                        buffer.rewind()
                        
                        // Update OpenGL texture
                        renderer.updateTexture(buffer, processedMat.cols(), processedMat.rows())
                        
                        // FPS calculation
                        frameCount++
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastFpsTime >= 1000) {
                            val fps = frameCount / ((currentTime - lastFpsTime) / 1000.0)
                            Log.d(TAG, "FPS: $fps")
                            frameCount = 0
                            lastFpsTime = currentTime
                        }
                        
                        // Clean up
                        NativeBridge.releaseMat(processedMatAddr)
                        yuvMat.release()
                        rgbaMat.release()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing frame", e)
                    } finally {
                        it.close()
                    }
                }
            }
            startCamera(640, 480)
        }
        
        Log.d(TAG, "Camera started")
    }
    
    private fun imageToMat(image: android.media.Image): Mat {
        val yPlane = image.planes[0]
        val yBuffer = yPlane.buffer
        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(ySize + image.width * image.height / 2)
        
        yBuffer.get(nv21, 0, ySize)
        
        // Interleave U and V planes
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        var pos = ySize
        var i = 0
        while (i < uBuffer.remaining()) {
            nv21[pos++] = vBuffer.get(i)
            nv21[pos++] = uBuffer.get(i)
            i += 2  // Skip pixel for subsampling
        }
        
        val mat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        mat.put(0, 0, nv21)
        return mat
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraManager.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupViews()
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        if (::cameraManager.isInitialized) {
            cameraManager.stopCamera()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (::glSurfaceView.isInitialized) {
            glSurfaceView.onResume()
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}


