package com.agri.assistant.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.agri.assistant.R
import com.agri.assistant.db.ReportDao
import com.agri.assistant.ml.SoilNutrientAnalyzer
import com.agri.assistant.model.HistoryItem
import com.agri.assistant.model.SoilReport
import com.agri.assistant.utils.ImageUtils
import com.agri.assistant.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SoilNutrientActivity : AppCompatActivity() {

    private lateinit var analyzer: SoilNutrientAnalyzer
    private lateinit var reportDao: ReportDao
    private lateinit var session: SessionManager
    private lateinit var resultSection: LinearLayout
    private var currentResult: SoilNutrientAnalyzer.NutrientResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soil_nutrient)

        analyzer = SoilNutrientAnalyzer()
        reportDao = ReportDao(this)
        session = SessionManager(this)
        resultSection = findViewById(R.id.resultSection)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnAnalyze).setOnClickListener { analyzeNutrients() }
        findViewById<MaterialButton>(R.id.btnSaveReport).setOnClickListener { saveReport() }
    }

    private fun analyzeNutrients() {
        val nStr = findViewById<TextInputEditText>(R.id.etNitrogen).text.toString()
        val pStr = findViewById<TextInputEditText>(R.id.etPhosphorus).text.toString()
        val kStr = findViewById<TextInputEditText>(R.id.etPotassium).text.toString()
        val phStr = findViewById<TextInputEditText>(R.id.etPH).text.toString()
        val moistureStr = findViewById<TextInputEditText>(R.id.etMoisture).text.toString()

        if (nStr.isEmpty() || pStr.isEmpty() || kStr.isEmpty() || phStr.isEmpty() || moistureStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val n = nStr.toFloatOrNull() ?: 0f
        val p = pStr.toFloatOrNull() ?: 0f
        val k = kStr.toFloatOrNull() ?: 0f
        val ph = phStr.toFloatOrNull() ?: 7f
        val moisture = moistureStr.toFloatOrNull() ?: 50f

        try {
            val result = analyzer.analyze(n, p, k, ph, moisture)
            currentResult = result
            displayResult(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error analyzing nutrients: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayResult(result: SoilNutrientAnalyzer.NutrientResult) {
        resultSection.visibility = View.VISIBLE

        val statusEmoji = when (result.fertilityStatus) {
            "Excellent" -> "🟢"
            "Good" -> "🟡"
            "Average" -> "🟠"
            else -> "🔴"
        }

        findViewById<TextView>(R.id.tvFertilityStatus).text = "$statusEmoji ${result.fertilityStatus}"
        findViewById<TextView>(R.id.tvFertilityDesc).text = result.fertilityDescription
        findViewById<TextView>(R.id.tvDetailedAnalysis).text = result.detailedAnalysis
        findViewById<TextView>(R.id.tvFertilizers).text =
            result.recommendedFertilizers.joinToString("\n") { "• $it" }
        findViewById<TextView>(R.id.tvCrops).text =
            result.suitableCrops.joinToString("\n") { "• $it" }
    }

    private fun saveReport() {
        val result = currentResult ?: return
        val dateTime = ImageUtils.getCurrentDateTime()

        val report = SoilReport(
            userId = session.getUserId(),
            soilType = "Nutrient Analysis",
            nutrientStatus = result.fertilityStatus,
            recommendation = result.recommendedFertilizers.joinToString(", "),
            suitableCrops = result.suitableCrops.joinToString(", "),
            date = dateTime
        )

        val reportId = reportDao.insertSoilReport(report)
        if (reportId > 0) {
            reportDao.insertHistory(
                HistoryItem(
                    userId = session.getUserId(),
                    reportType = "soil_nutrient",
                    reportId = reportId,
                    summary = "Nutrient Analysis: ${result.fertilityStatus}",
                    timestamp = dateTime
                )
            )
            Toast.makeText(this, getString(R.string.report_saved), Toast.LENGTH_SHORT).show()
        }
    }
}
