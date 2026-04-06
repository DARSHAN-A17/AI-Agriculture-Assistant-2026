package com.agri.assistant.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.agri.assistant.model.PlantReport
import com.agri.assistant.model.SoilReport
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfUtils {

    fun exportPlantReport(context: Context, report: PlantReport) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 16f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }

        var yPos = 50f
        canvas.drawText("🌱 Plant Disease Analysis Report", 50f, yPos, titlePaint)
        yPos += 40f
        
        canvas.drawText("Date: ${report.date}", 50f, yPos, textPaint)
        yPos += 40f
        
        canvas.drawText("Plant: ", 50f, yPos, boldPaint)
        canvas.drawText(report.plantName, 120f, yPos, textPaint)
        yPos += 30f
        
        canvas.drawText("Status/Disease: ", 50f, yPos, boldPaint)
        canvas.drawText(report.diseaseName, 180f, yPos, textPaint)
        yPos += 40f

        // Draw image if available
        if (report.imagePath.isNotEmpty()) {
            val file = File(report.imagePath)
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(report.imagePath)
                    // Scale bitmap to fit
                    val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                    canvas.drawBitmap(scaled, 50f, yPos, null)
                    yPos += 220f
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        canvas.drawText("Description:", 50f, yPos, boldPaint)
        yPos += 20f
        yPos = drawTextMultiline(canvas, report.diseaseDescription, 50f, yPos, textPaint, 500f)
        yPos += 20f

        canvas.drawText("Cause:", 50f, yPos, boldPaint)
        yPos += 20f
        yPos = drawTextMultiline(canvas, report.cause, 50f, yPos, textPaint, 500f)
        yPos += 20f

        canvas.drawText("Treatment:", 50f, yPos, boldPaint)
        yPos += 20f
        yPos = drawTextMultiline(canvas, report.treatment, 50f, yPos, textPaint, 500f)
        yPos += 20f

        canvas.drawText("Prevention:", 50f, yPos, boldPaint)
        yPos += 20f
        drawTextMultiline(canvas, report.prevention, 50f, yPos, textPaint, 500f)

        document.finishPage(page)
        
        val filename = "Plant_Report_${System.currentTimeMillis()}.pdf"
        savePdfToDownloads(context, document, filename)
        document.close()
    }

    fun exportSoilReport(context: Context, report: SoilReport) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 16f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }

        var yPos = 50f
        canvas.drawText("🏔️ Soil Analysis Report", 50f, yPos, titlePaint)
        yPos += 40f
        
        canvas.drawText("Date: ${report.date}", 50f, yPos, textPaint)
        yPos += 40f
        
        canvas.drawText("Analysis Type/Soil: ", 50f, yPos, boldPaint)
        canvas.drawText(report.soilType, 200f, yPos, textPaint)
        yPos += 30f
        
        canvas.drawText("Status: ", 50f, yPos, boldPaint)
        canvas.drawText(report.nutrientStatus, 120f, yPos, textPaint)
        yPos += 40f

        // Draw image if available
        if (report.imagePath.isNotEmpty()) {
            val file = File(report.imagePath)
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(report.imagePath)
                    val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                    canvas.drawBitmap(scaled, 50f, yPos, null)
                    yPos += 220f
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        canvas.drawText("Recommendations / Fertilizers:", 50f, yPos, boldPaint)
        yPos += 20f
        yPos = drawTextMultiline(canvas, report.recommendation.replace("• ", ""), 50f, yPos, textPaint, 500f)
        yPos += 20f

        if (report.suitableCrops.isNotEmpty()) {
            canvas.drawText("Suitable Crops:", 50f, yPos, boldPaint)
            yPos += 20f
            drawTextMultiline(canvas, report.suitableCrops.replace("• ", ""), 50f, yPos, textPaint, 500f)
        }

        document.finishPage(page)
        
        val filename = "Soil_Report_${System.currentTimeMillis()}.pdf"
        savePdfToDownloads(context, document, filename)
        document.close()
    }

    private fun drawTextMultiline(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Float): Float {
        var currentY = y
        val words = text.split(" ")
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = word
                currentY += paint.textSize + 8f
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
            currentY += paint.textSize + 8f
        }
        return currentY
    }

    private fun savePdfToDownloads(context: Context, document: PdfDocument, filename: String) {
        try {
            var out: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    out = context.contentResolver.openOutputStream(uri)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, filename)
                out = FileOutputStream(file)
            }

            out?.use {
                document.writeTo(it)
                Toast.makeText(context, "Report downloaded to Files (Downloads)", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(context, "Failed to create PDF file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}