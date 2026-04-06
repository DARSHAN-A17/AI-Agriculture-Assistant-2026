package com.agri.assistant.db

import android.content.ContentValues
import android.content.Context
import com.agri.assistant.model.HistoryItem
import com.agri.assistant.model.PlantReport
import com.agri.assistant.model.SoilReport

class ReportDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertPlantReport(report: PlantReport): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", report.userId)
            put(DatabaseHelper.COLUMN_IMAGE_PATH, report.imagePath)
            put(DatabaseHelper.COLUMN_PLANT_NAME, report.plantName)
            put(DatabaseHelper.COLUMN_DISEASE_NAME, report.diseaseName)
            put(DatabaseHelper.COLUMN_DESCRIPTION, report.diseaseDescription)
            put(DatabaseHelper.COLUMN_CAUSE, report.cause)
            put(DatabaseHelper.COLUMN_TREATMENT, report.treatment)
            put(DatabaseHelper.COLUMN_PREVENTION, report.prevention)
            put(DatabaseHelper.COLUMN_DATE, report.date)
        }
        return db.insert(DatabaseHelper.TABLE_PLANT_REPORTS, null, values)
    }

    fun insertSoilReport(report: SoilReport): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", report.userId)
            put(DatabaseHelper.COLUMN_IMAGE_PATH, report.imagePath)
            put(DatabaseHelper.COLUMN_SOIL_TYPE, report.soilType)
            put(DatabaseHelper.COLUMN_NUTRIENT_STATUS, report.nutrientStatus)
            put(DatabaseHelper.COLUMN_RECOMMENDATION, report.recommendation)
            put(DatabaseHelper.COLUMN_SUITABLE_CROPS, report.suitableCrops)
            put(DatabaseHelper.COLUMN_DATE, report.date)
        }
        return db.insert(DatabaseHelper.TABLE_SOIL_REPORTS, null, values)
    }

    fun insertHistory(item: HistoryItem): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", item.userId)
            put(DatabaseHelper.COLUMN_REPORT_TYPE, item.reportType)
            put(DatabaseHelper.COLUMN_REPORT_ID, item.reportId)
            put(DatabaseHelper.COLUMN_SUMMARY, item.summary)
            put(DatabaseHelper.COLUMN_TIMESTAMP, item.timestamp)
        }
        return db.insert(DatabaseHelper.TABLE_HISTORY, null, values)
    }

    fun getPlantReports(userId: Long): List<PlantReport> {
        val reports = mutableListOf<PlantReport>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PLANT_REPORTS, null, "user_id=?",
            arrayOf(userId.toString()), null, null, "${DatabaseHelper.COLUMN_ID} DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                reports.add(
                    PlantReport(
                        id = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        userId = getLong(getColumnIndexOrThrow("user_id")),
                        imagePath = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH)),
                        plantName = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLANT_NAME)),
                        diseaseName = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISEASE_NAME)),
                        diseaseDescription = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                        cause = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAUSE)),
                        treatment = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TREATMENT)),
                        prevention = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREVENTION)),
                        date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                    )
                )
            }
            close()
        }
        return reports
    }

    fun getSoilReports(userId: Long): List<SoilReport> {
        val reports = mutableListOf<SoilReport>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SOIL_REPORTS, null, "user_id=?",
            arrayOf(userId.toString()), null, null, "${DatabaseHelper.COLUMN_ID} DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                reports.add(
                    SoilReport(
                        id = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        userId = getLong(getColumnIndexOrThrow("user_id")),
                        imagePath = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_PATH)),
                        soilType = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SOIL_TYPE)),
                        nutrientStatus = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUTRIENT_STATUS)),
                        recommendation = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECOMMENDATION)),
                        suitableCrops = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUITABLE_CROPS)),
                        date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                    )
                )
            }
            close()
        }
        return reports
    }

    fun getHistory(userId: Long): List<HistoryItem> {
        val items = mutableListOf<HistoryItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_HISTORY, null, "user_id=?",
            arrayOf(userId.toString()), null, null, "${DatabaseHelper.COLUMN_ID} DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                items.add(
                    HistoryItem(
                        id = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        userId = getLong(getColumnIndexOrThrow("user_id")),
                        reportType = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_TYPE)),
                        reportId = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPORT_ID)),
                        summary = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUMMARY)),
                        timestamp = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP))
                    )
                )
            }
            close()
        }
        return items
    }
}
