package com.agri.assistant.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.agri.assistant.R
import com.agri.assistant.db.ReportDao
import com.agri.assistant.model.HistoryItem
import com.agri.assistant.model.PlantReport
import com.agri.assistant.model.SoilReport
import com.agri.assistant.utils.ImageUtils
import com.agri.assistant.utils.SessionManager
import com.google.android.material.button.MaterialButton
import java.io.File

class ReportsActivity : AppCompatActivity() {

    private lateinit var reportDao: ReportDao
    private lateinit var session: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        reportDao = ReportDao(this)
        session = SessionManager(this)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val btnPlant = findViewById<MaterialButton>(R.id.btnPlantReports)
        val btnSoil = findViewById<MaterialButton>(R.id.btnSoilReports)
        val btnHistory = findViewById<MaterialButton>(R.id.btnHistory)

        btnPlant.setOnClickListener {
            showPlantReports()
        }

        btnSoil.setOnClickListener {
            showSoilReports()
        }

        btnHistory.setOnClickListener {
            showHistory()
        }

        // Default: show plant reports
        showPlantReports()
    }

    private fun showPlantReports() {
        val reports = reportDao.getPlantReports(session.getUserId())
        if (reports.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.no_reports)
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = PlantReportAdapter(reports)
        }
    }

    private fun showSoilReports() {
        val reports = reportDao.getSoilReports(session.getUserId())
        if (reports.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.no_reports)
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = SoilReportAdapter(reports)
        }
    }

    private fun showHistory() {
        val items = reportDao.getHistory(session.getUserId())
        if (items.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = getString(R.string.no_history)
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = HistoryAdapter(items)
        }
    }

    // Plant Report Adapter
    inner class PlantReportAdapter(private val reports: List<PlantReport>) :
        RecyclerView.Adapter<PlantReportAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivReportImage)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val report = reports[position]
            holder.tvTitle.text = "🌱 ${report.plantName}"
            holder.tvSubtitle.text = report.diseaseName
            holder.tvDate.text = ImageUtils.formatDate(report.date)

            if (report.imagePath.isNotEmpty() && File(report.imagePath).exists()) {
                val bitmap = BitmapFactory.decodeFile(report.imagePath)
                holder.ivImage.setImageBitmap(bitmap)
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_plant)
            }

            holder.itemView.setOnClickListener {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("🌱 ${report.plantName} Analysis")
                    .setMessage("Disease: ${report.diseaseName}\n\nDescription:\n${report.diseaseDescription}\n\nCause:\n${report.cause}\n\nTreatment:\n${report.treatment}\n\nPrevention:\n${report.prevention}")
                    .setPositiveButton("Close", null)
                    .setNeutralButton("Download PDF") { _, _ ->
                        com.agri.assistant.utils.PdfUtils.exportPlantReport(holder.itemView.context, report)
                    }
                    .show()
            }
        }

        override fun getItemCount() = reports.size
    }

    // Soil Report Adapter
    inner class SoilReportAdapter(private val reports: List<SoilReport>) :
        RecyclerView.Adapter<SoilReportAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivReportImage)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val report = reports[position]
            holder.tvTitle.text = "🏔️ ${report.soilType}"
            holder.tvSubtitle.text = report.recommendation.take(50)
            holder.tvDate.text = ImageUtils.formatDate(report.date)

            if (report.imagePath.isNotEmpty() && File(report.imagePath).exists()) {
                val bitmap = BitmapFactory.decodeFile(report.imagePath)
                holder.ivImage.setImageBitmap(bitmap)
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_soil)
            }

            holder.itemView.setOnClickListener {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("🏔️ ${report.soilType}")
                    .setMessage("Status: ${report.nutrientStatus}\n\nRecommendations:\n${report.recommendation}\n\nSuitable Crops:\n${report.suitableCrops}")
                    .setPositiveButton("Close", null)
                    .setNeutralButton("Download PDF") { _, _ ->
                        com.agri.assistant.utils.PdfUtils.exportSoilReport(holder.itemView.context, report)
                    }
                    .show()
            }
        }

        override fun getItemCount() = reports.size
    }

    // History Adapter
    inner class HistoryAdapter(private val items: List<HistoryItem>) :
        RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivReportImage)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvTitle.text = item.summary
            holder.tvSubtitle.text = "Type: ${item.reportType.replace("_", " ").uppercase()}"
            holder.tvDate.text = ImageUtils.formatDate(item.timestamp)

            val iconRes = when (item.reportType) {
                "plant" -> R.drawable.ic_plant
                "soil_image" -> R.drawable.ic_soil
                else -> R.drawable.ic_nutrient
            }
            holder.ivImage.setImageResource(iconRes)
        }

        override fun getItemCount() = items.size
    }
}