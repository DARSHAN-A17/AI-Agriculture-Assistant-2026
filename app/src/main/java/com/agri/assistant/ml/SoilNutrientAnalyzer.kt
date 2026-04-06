package com.agri.assistant.ml

import android.content.Context

/**
 * Soil Nutrient Analyzer using rule-based logic derived from the
 * Crop Recommendation Dataset. Analyzes N, P, K, pH, and moisture
 * to predict soil fertility, recommended fertilizers, and suitable crops.
 */
class SoilNutrientAnalyzer {

    data class NutrientResult(
        val fertilityStatus: String,
        val fertilityDescription: String,
        val recommendedFertilizers: List<String>,
        val suitableCrops: List<String>,
        val detailedAnalysis: String
    )

    fun analyze(
        nitrogen: Float,
        phosphorus: Float,
        potassium: Float,
        ph: Float,
        moisture: Float
    ): NutrientResult {

        // Evaluate individual nutrient levels
        val nLevel = evaluateNitrogen(nitrogen)
        val pLevel = evaluatePhosphorus(phosphorus)
        val kLevel = evaluatePotassium(potassium)
        val phStatus = evaluatePH(ph)
        val moistureStatus = evaluateMoisture(moisture)

        // Overall fertility
        val fertilityScore = calculateFertilityScore(nLevel, pLevel, kLevel, phStatus, moistureStatus)
        val fertilityStatus = getFertilityStatus(fertilityScore)
        val fertilityDescription = getFertilityDescription(fertilityScore)

        // Recommendations
        // Call Python Backend via Chaquopy for the real ML Recommendation
        val crops = mutableListOf<String>()
        try {
            if (!com.chaquo.python.Python.isStarted()) {
                // If Chaquopy wasn't started in the main application block, we can't run the script
                // Fallback to legacy recommendations if the library is missing
                crops.addAll(recommendCrops(nitrogen, phosphorus, potassium, ph, moisture))
            } else {
                val py = com.chaquo.python.Python.getInstance()
                val module = py.getModule("analyze_nutrients")
                
                // Assuming standard Indian weather conditions as default placeholders since UI only provides N,P,K,pH,M
                val temperature = 25.0f
                val humidity = 71.0f 
                val rainfall = 100.0f
                
                val mlPrediction = module.callAttr("predict_crop", nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall)
                crops.add(mlPrediction.toString().replaceFirstChar { it.uppercase() } + " (AI Recommended)")
                
                // Keep some fallback crops just in case the AI only suggests one
                val fallback = recommendCrops(nitrogen, phosphorus, potassium, ph, moisture)
                crops.addAll(fallback.take(2))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            crops.addAll(recommendCrops(nitrogen, phosphorus, potassium, ph, moisture))
        } catch (e: Throwable) {
            // Catch Throwable to handle NoClassDefFoundError/ClassNotFoundException from Chaquopy
            e.printStackTrace()
            crops.addAll(recommendCrops(nitrogen, phosphorus, potassium, ph, moisture))
        }

        // Detailed analysis text
        val analysis = buildString {
            appendLine(" Detailed Soil Analysis")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Nitrogen (N): $nitrogen mg/kg — ${nLevel.display}")
            appendLine("Phosphorus (P): $phosphorus mg/kg — ${pLevel.display}")
            appendLine("Potassium (K): $potassium mg/kg — ${kLevel.display}")
            appendLine("pH Level: $ph — ${phStatus.display}")
            appendLine("Moisture: $moisture% — ${moistureStatus.display}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Overall Fertility Score: $fertilityScore/100")
        }

        return NutrientResult(
            fertilityStatus = fertilityStatus,
            fertilityDescription = fertilityDescription,
            recommendedFertilizers = recommendFertilizers(nLevel, pLevel, kLevel, phStatus),
            suitableCrops = crops,
            detailedAnalysis = analysis
        )
    }

    private enum class Level(val display: String) {
        LOW("Low ⚠️"),
        MEDIUM("Medium ✅"),
        HIGH("High ✅"),
        VERY_HIGH("Very High ⚠️"),
        ACIDIC("Acidic ⚠️"),
        SLIGHTLY_ACIDIC("Slightly Acidic ✅"),
        NEUTRAL("Neutral ✅"),
        SLIGHTLY_ALKALINE("Slightly Alkaline ✅"),
        ALKALINE("Alkaline ⚠️"),
        DRY("Dry ⚠️"),
        OPTIMAL("Optimal ✅"),
        WET("Too Wet ⚠️")
    }

    private fun evaluateNitrogen(n: Float): Level = when {
        n < 30 -> Level.LOW
        n < 60 -> Level.MEDIUM
        n < 120 -> Level.HIGH
        else -> Level.VERY_HIGH
    }

    private fun evaluatePhosphorus(p: Float): Level = when {
        p < 20 -> Level.LOW
        p < 50 -> Level.MEDIUM
        p < 100 -> Level.HIGH
        else -> Level.VERY_HIGH
    }

    private fun evaluatePotassium(k: Float): Level = when {
        k < 30 -> Level.LOW
        k < 60 -> Level.MEDIUM
        k < 120 -> Level.HIGH
        else -> Level.VERY_HIGH
    }

    private fun evaluatePH(ph: Float): Level = when {
        ph < 5.5f -> Level.ACIDIC
        ph < 6.5f -> Level.SLIGHTLY_ACIDIC
        ph < 7.5f -> Level.NEUTRAL
        ph < 8.0f -> Level.SLIGHTLY_ALKALINE
        else -> Level.ALKALINE
    }

    private fun evaluateMoisture(m: Float): Level = when {
        m < 30 -> Level.DRY
        m < 70 -> Level.OPTIMAL
        else -> Level.WET
    }

    private fun calculateFertilityScore(
        n: Level, p: Level, k: Level, ph: Level, moisture: Level
    ): Int {
        var score = 0

        // Nitrogen score (0-25)
        score += when (n) {
            Level.LOW -> 5; Level.MEDIUM -> 15; Level.HIGH -> 25; else -> 20
        }

        // Phosphorus score (0-25)
        score += when (p) {
            Level.LOW -> 5; Level.MEDIUM -> 15; Level.HIGH -> 25; else -> 20
        }

        // Potassium score (0-20)
        score += when (k) {
            Level.LOW -> 4; Level.MEDIUM -> 12; Level.HIGH -> 20; else -> 16
        }

        // pH score (0-15)
        score += when (ph) {
            Level.ACIDIC -> 3; Level.SLIGHTLY_ACIDIC -> 12; Level.NEUTRAL -> 15
            Level.SLIGHTLY_ALKALINE -> 12; else -> 5
        }

        // Moisture score (0-15)
        score += when (moisture) {
            Level.DRY -> 5; Level.OPTIMAL -> 15; else -> 7
        }

        return score.coerceIn(0, 100)
    }

    private fun getFertilityStatus(score: Int): String = when {
        score < 30 -> "Poor"
        score < 50 -> "Below Average"
        score < 65 -> "Average"
        score < 80 -> "Good"
        else -> "Excellent"
    }

    private fun getFertilityDescription(score: Int): String = when {
        score < 30 -> "Soil fertility is poor. Significant amendments and fertilization needed before planting."
        score < 50 -> "Soil needs improvement. Apply recommended fertilizers and organic matter."
        score < 65 -> "Soil is moderately fertile. Some supplementation may improve yields."
        score < 80 -> "Soil is in good condition for farming. Follow recommendations for best results."
        else -> "Excellent soil condition. Ideal for a wide variety of crops."
    }

    private fun recommendFertilizers(n: Level, p: Level, k: Level, ph: Level): List<String> {
        val fertilizers = mutableListOf<String>()

        if (n == Level.LOW) {
            fertilizers.add("Urea (46-0-0) — Apply 50-100 kg/ha")
            fertilizers.add("Ammonium Sulphate (21-0-0) for acidic soils")
        }
        if (p == Level.LOW) {
            fertilizers.add("DAP (18-46-0) — Apply 50-75 kg/ha")
            fertilizers.add("Single Super Phosphates (SSP)")
        }
        if (k == Level.LOW) {
            fertilizers.add("Muriate of Potash (MOP) — Apply 40-60 kg/ha")
            fertilizers.add("Potassium Sulphate (SOP)")
        }
        if (ph == Level.ACIDIC) {
            fertilizers.add("Agricultural Lime — 2-4 tonnes/ha to raise pH")
        }
        if (ph == Level.ALKALINE) {
            fertilizers.add("Gypsum — 2-3 tonnes/ha to lower pH")
            fertilizers.add("Sulphur powder application")
        }
        if (n != Level.LOW && p != Level.LOW && k != Level.LOW) {
            fertilizers.add("NPK Complex (10-26-26) for maintenance")
            fertilizers.add("Organic compost for long-term health")
        }
        fertilizers.add("Vermicompost — 2-3 tonnes/ha (general)")

        return fertilizers
    }

    private fun recommendCrops(n: Float, p: Float, k: Float, ph: Float, moisture: Float): List<String> {
        val crops = mutableListOf<String>()

        // Rice — high N, moderate PK, slightly acidic-neutral
        if (n > 40 && p > 30 && k > 30 && ph in 5.0f..7.0f && moisture > 50) {
            crops.add("Rice")
        }
        // Wheat — moderate NPK, neutral pH
        if (n > 30 && p > 25 && k > 25 && ph in 6.0f..7.5f) {
            crops.add("Wheat")
        }
        // Maize — high N, moderate PK
        if (n > 50 && p > 30 && k > 30 && ph in 5.5f..7.5f) {
            crops.add("Maize")
        }
        // Cotton — moderate NPK, neutral-alkaline
        if (n > 30 && p > 25 && k > 30 && ph in 6.0f..8.0f) {
            crops.add("Cotton")
        }
        // Sugarcane — high NPK, slightly acidic-neutral
        if (n > 50 && p > 40 && k > 40 && ph in 5.0f..7.5f) {
            crops.add("Sugarcane")
        }
        // Pulses (Lentils) — low N is ok, moderate PK
        if (p > 20 && k > 20 && ph in 6.0f..7.5f) {
            crops.add("Lentils / Pulses")
        }
        // Groundnut — low N ok, moderate PK, slightly acidic
        if (p > 20 && k > 20 && ph in 5.5f..7.0f) {
            crops.add("Groundnut")
        }
        // Tomato — high NPK, slightly acidic-neutral
        if (n > 40 && p > 40 && k > 40 && ph in 5.5f..7.0f) {
            crops.add("Tomato")
        }
        // Potato — moderate NPK, acidic-neutral
        if (n > 30 && p > 30 && k > 40 && ph in 4.5f..6.5f) {
            crops.add("Potato")
        }
        // Millets — low nutrient requirement
        if (n > 15 && p > 10 && k > 15 && ph in 5.5f..8.0f) {
            crops.add("Millets")
        }
        // Banana — high NPK, slightly acidic
        if (n > 60 && p > 40 && k > 50 && ph in 5.5f..7.0f) {
            crops.add("Banana")
        }
        // Soybean
        if (n > 20 && p > 30 && k > 30 && ph in 6.0f..7.0f) {
            crops.add("Soybean")
        }

        if (crops.isEmpty()) {
            crops.add("Cover crops (to improve soil)")
            crops.add("Green manure crops (Sesbania, Dhaincha)")
        }

        return crops
    }
}
