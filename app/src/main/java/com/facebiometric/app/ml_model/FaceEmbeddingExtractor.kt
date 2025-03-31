package com.facebiometric.app.ml_model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceEmbeddingExtractor(context: Context) {

    private var interpreter: Interpreter? = null

    init {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, "facenet.tflite") // Load model from assets
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            Log.e("FaceEmbeddingExtractor", "Failed to load TFLite model: ${e.message}")
        }
    }

    fun generateEmbedding(bitmap: Bitmap): FloatArray {
        if (interpreter == null) {
            throw IllegalStateException("TFLite Interpreter is not initialized!")
        }

        val processedBitmap = preprocessBitmap(bitmap) // Resize & normalize
        val inputBuffer = convertBitmapToBuffer(processedBitmap)
        val output = Array(1) { FloatArray(128) }  // Output embedding (e.g., 128D)

        interpreter?.run(inputBuffer, output)

        return output[0] // Return embedding vector
    }

    private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 160, 160, true) // Resize to 160x160
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(1 * 160 * 160 * 3 * 4) // 1 batch, 160x160, RGB, 4 bytes per float
        imgData.order(ByteOrder.nativeOrder())

        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel) / 255.0f
                val g = Color.green(pixel) / 255.0f
                val b = Color.blue(pixel) / 255.0f

                imgData.putFloat(r)
                imgData.putFloat(g)
                imgData.putFloat(b)
            }
        }
        return imgData
    }

    fun close() {
        interpreter?.close()
    }
}
