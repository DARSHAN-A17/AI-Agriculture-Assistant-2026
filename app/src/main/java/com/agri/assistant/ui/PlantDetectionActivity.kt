package com.agri.assistant.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.agri.assistant.R
import com.agri.assistant.db.ReportDao
import com.agri.assistant.ml.DiseaseKnowledgeBase
import com.agri.assistant.ml.PlantDiseaseClassifier
import com.agri.assistant.model.HistoryItem
import com.agri.assistant.model.PlantReport
import com.agri.assistant.utils.ImageUtils
import com.agri.assistant.utils.PdfUtils
import com.agri.assistant.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.io.File

class PlantDetectionActivity : AppCompatActivity() {

    private lateinit var classifier: PlantDiseaseClassifier
    private lateinit var reportDao: ReportDao
    private lateinit var session: SessionManager

    private lateinit var ivPreview: ImageView
    private lateinit var tvPlaceholder: TextView
    private lateinit var resultSection: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var selectedBitmap: Bitmap? = null
    private var savedImagePath: String = ""
    private var currentDiseaseInfo: DiseaseKnowledgeBase.DiseaseInfo? = null
    private var currentConfidence: Float = 0f
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bitmap = ImageUtils.loadBitmapFromUri(this, photoUri)
            bitmap?.let {
                selectedBitmap = it
                ivPreview.setImageBitmap(it)
                tvPlaceholder.visibility = View.GONE
                savedImagePath = photoFile.absolutePath
                resultSection.visibility = View.GONE
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = ImageUtils.loadBitmapFromUri(this, it)
            bitmap?.let { bmp ->
                selectedBitmap = bmp
                ivPreview.setImageBitmap(bmp)
                tvPlaceholder.visibility = View.GONE
                savedImagePath = ImageUtils.saveBitmapToFile(this, bmp, "plant")
                resultSection.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detection)

        classifier = PlantDiseaseClassifier(this)
        reportDao = ReportDao(this)
        session = SessionManager(this)

        ivPreview = findViewById(R.id.ivPreview)
        tvPlaceholder = findViewById(R.id.tvPlaceholder)
        resultSection = findViewById(R.id.resultSection)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        findViewById<MaterialButton>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.btnAnalyze).setOnClickListener {
            analyzeImage()
        }

        findViewById<MaterialButton>(R.id.btnSaveReport).setOnClickListener {
            saveReport()
        }
    }

    private fun launchCamera() {
        val dir = File(getExternalFilesDir(null), "Pictures")
        if (!dir.exists()) dir.mkdirs()
        photoFile = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        cameraLauncher.launch(photoUri)
    }

    private fun analyzeImage() {
        val bitmap = selectedBitmap
        if (bitmap == null) {
            Toast.makeText(this, getString(R.string.select_image_first), Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        resultSection.visibility = View.GONE

        // Run inference in background thread
        Thread {
            try {
                val result = classifier.classify(bitmap)
                val diseaseInfo = DiseaseKnowledgeBase.getDiseaseInfo(result.label)
                currentDiseaseInfo = diseaseInfo
                currentConfidence = result.confidence

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    displayResult(diseaseInfo, result.confidence)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "An error occurred during analysis", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun displayResult(info: DiseaseKnowledgeBase.DiseaseInfo, confidence: Float) {
        resultSection.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvPlantName).text = "🌱 ${info.plantName}"
        findViewById<TextView>(R.id.tvDiseaseName).text = info.diseaseName

        val confPercent = (confidence * 100).toInt()
        val confColor = when {
            confPercent > 80 -> ContextCompat.getColor(this, R.color.status_healthy)
            confPercent > 50 -> ContextCompat.getColor(this, R.color.status_warning)
            else -> ContextCompat.getColor(this, R.color.status_danger)
        }
        val tvConf = findViewById<TextView>(R.id.tvConfidence)
        tvConf.text = "Confidence: $confPercent%"
        tvConf.setTextColor(confColor)

        if (info.diseaseName == "Healthy") {
            findViewById<TextView>(R.id.tvDiseaseName).setTextColor(
                ContextCompat.getColor(this, R.color.status_healthy)
            )
        }

        findViewById<TextView>(R.id.tvDescription).text = info.description
        findViewById<TextView>(R.id.tvCause).text = info.cause
        findViewById<TextView>(R.id.tvTreatment).text = info.treatment
        findViewById<TextView>(R.id.tvPrevention).text = info.prevention
    }

    private fun saveReport() {
        val info = currentDiseaseInfo ?: return
        val dateTime = ImageUtils.getCurrentDateTime()

        val report = PlantReport(
            userId = session.getUserId(),
            imagePath = savedImagePath,
            plantName = info.plantName,
            diseaseName = info.diseaseName,
            diseaseDescription = info.description,
            cause = info.cause,
            treatment = info.treatment,
            prevention = info.prevention,
            date = dateTime
        )

        val reportId = reportDao.insertPlantReport(report)
        if (reportId > 0) {
            reportDao.insertHistory(
                HistoryItem(
                    userId = session.getUserId(),
                    reportType = "plant",
                    reportId = reportId,
                    summary = "${info.plantName} - ${info.diseaseName}",
                    timestamp = dateTime
                )
            )
            Toast.makeText(this, getString(R.string.report_saved), Toast.LENGTH_SHORT).show()
            // Export to PDF
            PdfUtils.exportPlantReport(this, report)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}