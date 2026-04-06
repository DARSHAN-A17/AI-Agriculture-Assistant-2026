package com.agri.assistant.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PlantDiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 224
    private val pixelSize = 3
    private val labels = mutableListOf<String>()

    init {
        try {
            interpreter = Interpreter(loadModelFile("plant_disease_model.tflite"))
            loadLabels("plant_labels.txt")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(filename: String) {
        try {
            context.assets.open(filename).bufferedReader().useLines { lines ->
                labels.addAll(lines.filter { it.isNotBlank() }.toList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (interpreter == null || labels.isEmpty()) {
            return ClassificationResult("Unknown", 0f, -1)
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val output = Array(1) { FloatArray(labels.size) }

        try {
            interpreter?.run(byteBuffer, output)
        } catch (e: Exception) {
            e.printStackTrace()
            return ClassificationResult("Unknown", 0f, -1)
        }

        val probabilities = output[0]
        if (probabilities.isEmpty()) {
            return ClassificationResult("Unknown", 0f, -1)
        }

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIndex]

        return ClassificationResult(
            label = if (maxIndex < labels.size) labels[maxIndex] else "Unknown",
            confidence = confidence,
            classIndex = maxIndex
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        return byteBuffer
    }

    fun close() {
        interpreter?.close()
    }

    data class ClassificationResult(
        val label: String,
        val confidence: Float,
        val classIndex: Int
    )
}
