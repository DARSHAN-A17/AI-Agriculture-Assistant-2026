package com.agri.assistant.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SoilImageClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 224
    private val pixelSize = 3
    private val labels = mutableListOf<String>()

    init {
        try {
            interpreter = Interpreter(loadModelFile("soil_image_model.tflite"))
            loadLabels("soil_labels.txt")
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
            return ClassificationResult("Unknown", 0f)
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val output = Array(1) { FloatArray(labels.size) }

        try {
            interpreter?.run(byteBuffer, output)
        } catch (e: Exception) {
            e.printStackTrace()
            return ClassificationResult("Unknown", 0f)
        }

        val probabilities = output[0]
        if (probabilities.isEmpty()) {
            return ClassificationResult("Unknown", 0f)
        }

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIndex]

        return ClassificationResult(
            label = if (maxIndex < labels.size) labels[maxIndex] else "Unknown",
            confidence = confidence
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
        val confidence: Float
    )

    companion object {
        fun getSoilInfo(soilType: String): SoilInfo {
            return when (soilType.lowercase().replace("_", " ")) {
                "alluvial soil", "alluvial" -> SoilInfo(
                    type = "Alluvial Soil",
                    description = "Alluvial soil is the most fertile and widely spread soil. It is formed by the deposition of river sediments.",
                    characteristics = "Rich in potash and humus but poor in phosphorus. Very fertile, well-draining, and light.",
                    suitableCrops = "Rice, Wheat, Sugarcane, Cotton, Jute, Maize, Pulses, Oilseeds",
                    improvements = "Maintain with balanced NPK fertilizers and crop rotation. Generally requires less intervention."
                )
                "arid soil", "arid" -> SoilInfo(
                    type = "Arid Soil",
                    description = "Arid soil is found in dry regions. It is sandy, low in organic matter, and often saline.",
                    characteristics = "Sandy texture, poor in nitrogen, rich in calcium, highly alkaline, low moisture retention.",
                    suitableCrops = "Millets, Barley, Cotton, Maize, Pulses (with irrigation)",
                    improvements = "Requires heavy irrigation, addition of organic manure (humus), and gypsum to reduce alkalinity."
                )
                "black soil", "black" -> SoilInfo(
                    type = "Black Soil",
                    description = "Black soil (Regur soil) is rich in clay and remains moist for long. It swells when wet and shrinks when dry.",
                    characteristics = "High clay content, moisture retentive, self-ploughing, rich in calcium, potassium and magnesium, poor in nitrogen.",
                    suitableCrops = "Cotton, Soybeans, Wheat, Sugarcane, Sunflower, Millets",
                    improvements = "Improve drainage, add organic matter during dry season, use nitrogenous fertilizers."
                )
                "laterite soil", "laterite" -> SoilInfo(
                    type = "Laterite Soil",
                    description = "Laterite soil is formed under high temperature and heavy rainfall. It is rich in iron and aluminum but poor in organic matter.",
                    characteristics = "Acidic, lacks nitrogen and potash, prone to leaching, hardens like a brick when dry.",
                    suitableCrops = "Tea, Coffee, Rubber, Cashews, Tapioca, Coconut",
                    improvements = "Requires heavy application of fertilizers and manure. Add lime to neutralize acidity."
                )
                "mountain soil", "mountain" -> SoilInfo(
                    type = "Mountain Soil",
                    description = "Mountain soil (Forest soil) varies depending on the altitude. It is generally rich in humus but deficient in potash, phosphorus, and lime.",
                    characteristics = "Rich in organic matter, acidic in nature, loamy and silty on valley sides.",
                    suitableCrops = "Tea, Coffee, Spices, Tropical fruits (Apples, Pears, Plums)",
                    improvements = "Requires liming to reduce acidity, and terracing to prevent soil erosion."
                )
                "red soil", "red" -> SoilInfo(
                    type = "Red Soil",
                    description = "Red soil is rich in iron oxide, giving it a distinctive red color. Common in warm, temperate, and moist climates.",
                    characteristics = "Iron-rich, acidic, well-draining, low in nitrogen, phosphorus, and humus.",
                    suitableCrops = "Groundnuts, Pulses, Millets, Tobacco, Potatoes, Fruits",
                    improvements = "Add lime to reduce acidity, use green manure, apply nitrogenous and phosphatic fertilizers."
                )
                "yellow soil", "yellow" -> SoilInfo(
                    type = "Yellow Soil",
                    description = "Yellow soil is similar to red soil but is highly hydrated. It is generally less fertile.",
                    characteristics = "Hydrated iron oxide gives it a yellow color. Acidic, poor in nitrogen and phosphorus, moderate drainage.",
                    suitableCrops = "Paddy, Sugarcane, Groundnut, Mango, Citrus fruits",
                    improvements = "Requires heavy fertilization (NPK) and organic compost to boost fertility. Add lime if highly acidic."
                )
                else -> SoilInfo(
                    type = soilType,
                    description = "Soil type detected. Please consult local agriculture experts for detailed analysis.",
                    characteristics = "Characteristics data not available for this soil type.",
                    suitableCrops = "Consult local expert",
                    improvements = "Consult local expert"
                )
            }
        }
    }

    data class SoilInfo(
        val type: String,
        val description: String,
        val characteristics: String,
        val suitableCrops: String,
        val improvements: String
    )
}
