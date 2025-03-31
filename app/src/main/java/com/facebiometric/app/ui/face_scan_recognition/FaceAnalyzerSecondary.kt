package com.facebiometric.app.ui.face_scan_recognition

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.facebiometric.app.ui.face_scan_registration.FaceOverLay

class FaceAnalyzerSecondary(private val overlay: FaceOverLay,
                   private val onFaceCaptured: (Bitmap) -> Unit,
                   private val onProgressUpdate: (Int, String) -> Unit,
                   private val isFrontCamera: Boolean) : ImageAnalysis.Analyzer {

    private val capturedFaces = mutableListOf<Bitmap>()
    private var lastCaptureTime = 0L  // Timestamp for last capture
    private val captureInterval = 1000L  // 1-second interval per capture

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, 0)

            val imageWidth = mediaImage.width.toFloat()
            val imageHeight = mediaImage.height.toFloat()

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty() && capturedFaces.size < 4) {

                        val face = faces[0]
                        val boundingBox = face.boundingBox

                        // Convert bounding box to screen coordinates dynamically
                        val transformedRect = mapBoundingBoxToView(boundingBox, imageWidth, imageHeight, overlay.width, overlay.height, isFrontCamera)
                        // Update overlay with transformed face coordinates
                        overlay.setFaceBounds(transformedRect)

                        val currentTime = System.currentTimeMillis()

                        // Check if enough time has passed since last capture
                        if (currentTime - lastCaptureTime >= captureInterval) {
                            val face1 = faces[0]
                            val boundingBox1 = face1.boundingBox

                            val bitmap = imageProxy.toBitmap()
                            val faceBitmap = cropFace(bitmap, boundingBox1)

                            capturedFaces.add(faceBitmap)
                            onFaceCaptured(faceBitmap)

                            lastCaptureTime = currentTime  // Update timestamp

                            // Update progress
                            val progress = capturedFaces.size * 25
                            val message = when (capturedFaces.size) {
                                1 -> "Face detected. Hold still..."
                                2 -> "Face detected. Hold still..."
                                3 -> "Face detected. Hold still..."
                                else -> "Verification Completed!"
                            }
                            onProgressUpdate(progress, message)

                            if (capturedFaces.size == 4) {
                                Log.d("FaceAnalyzer", "All faces captured within time limit!")
                            }
                        }

                    } else {
                        overlay.setFaceBounds(null) // Clear if no face detected
                    }
                }
                .addOnFailureListener { Log.e("FaceAnalyzer", "Face detection failed", it) }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun cropFace(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        val x = boundingBox.left.coerceIn(0, bitmap.width) // Ensure x is within bounds
        val y = boundingBox.top.coerceIn(0, bitmap.height) // Ensure y is within bounds

        val width = boundingBox.width().coerceAtMost(bitmap.width - x)  // Ensure width fits
        val height = boundingBox.height().coerceAtMost(bitmap.height - y) // Ensure height fits

        return Bitmap.createBitmap(bitmap, x, y, width, height)
    }
    /*  private fun mapBoundingBoxToView(boundingBox: android.graphics.Rect, viewWidth: Int, viewHeight: Int): RectF {
          val scaleX = viewWidth.toFloat() / 400  // Adjust based on image size
          val scaleY = viewHeight.toFloat() / 600

          val left = boundingBox.left * scaleX
          val top = boundingBox.top * scaleY
          val right = boundingBox.right * scaleX
          val bottom = boundingBox.bottom * scaleY

          return RectF(left, top, right, bottom)
      }

     */

    private fun mapBoundingBoxToView(boundingBox: Rect, imageWidth: Float, imageHeight: Float, viewWidth: Int, viewHeight: Int, isFrontCamera: Boolean): RectF {
        // Dynamic scaling based on image size vs. view size
        val scaleX = viewWidth / imageWidth
        val scaleY = viewHeight / imageHeight

        var left = boundingBox.left * scaleX
        val top = boundingBox.top * scaleY
        var right = boundingBox.right * scaleX
        val bottom = boundingBox.bottom * scaleY

        // If front camera, mirror the X-axis (flip horizontally)
        if (isFrontCamera) {
            val flippedLeft = viewWidth - right
            val flippedRight = viewWidth - left
            left = flippedLeft
            right = flippedRight
        }

        return RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }
}