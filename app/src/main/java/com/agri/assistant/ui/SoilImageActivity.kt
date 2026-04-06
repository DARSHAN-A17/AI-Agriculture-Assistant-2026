package com.agri.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import com.agri.assistant.ml.SoilImageClassifier
import com.agri.assistant.model.HistoryItem
import com.agri.assistant.model.SoilReport
import com.agri.assistant.utils.ImageUtils
import com.agri.assistant.utils.PdfUtils
import com.agri.assistant.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.io.File

class SoilImageActivity : AppCompatActivity() {

    private lateinit var classifier: SoilImageClassifier
    private lateinit var reportDao: ReportDao
    private lateinit var session: SessionManager

    private lateinit var ivPreview: ImageView
    private lateinit var tvPlaceholder: TextView
    private lateinit var resultSection: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var selectedBitmap: Bitmap? = null
    private var savedImagePath: String = ""
    private var currentSoilInfo: SoilImageClassifier.SoilInfo? = null
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

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
                savedImagePath = ImageUtils.saveBitmapToFile(this, bmp, "soil")
                resultSection.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soil_image)

        classifier = SoilImageClassifier(this)
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

        findViewById<MaterialButton>(R.id.btnAnalyze).setOnClickListener { analyzeImage() }
        findViewById<MaterialButton>(R.id.btnSaveReport).setOnClickListener { saveReport() }
    }

    private fun launchCamera() {
        val dir = File(getExternalFilesDir(null), "Pictures")
        if (!dir.exists()) dir.mkdirs()
        photoFile = File(dir, "soil_${System.currentTimeMillis()}.jpg")
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

        Thread {
            try {
                val result = classifier.classify(bitmap)
                val soilInfo = SoilImageClassifier.getSoilInfo(result.label)
                currentSoilInfo = soilInfo

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    displayResult(soilInfo, result.confidence)
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

    private fun displayResult(info: SoilImageClassifier.SoilInfo, confidence: Float) {
        resultSection.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvSoilType).text = "🏔️ ${info.type}"
        val confPercent = (confidence * 100).toInt()
        findViewById<TextView>(R.id.tvConfidence).text = "Confidence: $confPercent%"
        findViewById<TextView>(R.id.tvDescription).text = info.description
        findViewById<TextView>(R.id.tvCharacteristics).text = info.characteristics
        findViewById<TextView>(R.id.tvCrops).text = info.suitableCrops
        findViewById<TextView>(R.id.tvImprovements).text = info.improvements
    }

    private fun saveReport() {
        val info = currentSoilInfo ?: return
        val dateTime = ImageUtils.getCurrentDateTime()

        val report = SoilReport(
            userId = session.getUserId(),
            imagePath = savedImagePath,
            soilType = info.type,
            nutrientStatus = info.characteristics,
            recommendation = info.improvements,
            suitableCrops = info.suitableCrops,
            date = dateTime
        )

        val reportId = reportDao.insertSoilReport(report)
        if (reportId > 0) {
            reportDao.insertHistory(
                HistoryItem(
                    userId = session.getUserId(),
                    reportType = "soil_image",
                    reportId = reportId,
                    summary = "Soil: ${info.type}",
                    timestamp = dateTime
                )
            )
            Toast.makeText(this, getString(R.string.report_saved), Toast.LENGTH_SHORT).show()
            PdfUtils.exportSoilReport(this, report)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}