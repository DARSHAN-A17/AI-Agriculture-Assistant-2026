package com.agri.assistant.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "agri_assistant.db"
        private const val DATABASE_VERSION = 1

        // User table
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_PASSWORD = "password"

        // Plant Reports table
        const val TABLE_PLANT_REPORTS = "plant_reports"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_PLANT_NAME = "plant_name"
        const val COLUMN_DISEASE_NAME = "disease_name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CAUSE = "cause"
        const val COLUMN_TREATMENT = "treatment"
        const val COLUMN_PREVENTION = "prevention"
        const val COLUMN_DATE = "date"

        // Soil Reports table
        const val TABLE_SOIL_REPORTS = "soil_reports"
        const val COLUMN_SOIL_TYPE = "soil_type"
        const val COLUMN_NUTRIENT_STATUS = "nutrient_status"
        const val COLUMN_RECOMMENDATION = "recommendation"
        const val COLUMN_SUITABLE_CROPS = "suitable_crops"

        // History table
        const val TABLE_HISTORY = "history"
        const val COLUMN_REPORT_TYPE = "report_type" // plant, soil_image, soil_nutrient
        const val COLUMN_REPORT_ID = "report_id"
        const val COLUMN_SUMMARY = "summary"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_PHONE TEXT UNIQUE, " +
                "$COLUMN_LOCATION TEXT, " +
                "$COLUMN_PASSWORD TEXT)")

        val createPlantReportsTable = ("CREATE TABLE $TABLE_PLANT_REPORTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "$COLUMN_IMAGE_PATH TEXT, " +
                "$COLUMN_PLANT_NAME TEXT, " +
                "$COLUMN_DISEASE_NAME TEXT, " +
                "$COLUMN_DESCRIPTION TEXT, " +
                "$COLUMN_CAUSE TEXT, " +
                "$COLUMN_TREATMENT TEXT, " +
                "$COLUMN_PREVENTION TEXT, " +
                "$COLUMN_DATE TEXT)")

        val createSoilReportsTable = ("CREATE TABLE $TABLE_SOIL_REPORTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "$COLUMN_IMAGE_PATH TEXT, " +
                "$COLUMN_SOIL_TYPE TEXT, " +
                "$COLUMN_NUTRIENT_STATUS TEXT, " +
                "$COLUMN_RECOMMENDATION TEXT, " +
                "$COLUMN_SUITABLE_CROPS TEXT, " +
                "$COLUMN_DATE TEXT)")

        val createHistoryTable = ("CREATE TABLE $TABLE_HISTORY (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "$COLUMN_REPORT_TYPE TEXT, " +
                "$COLUMN_REPORT_ID INTEGER, " +
                "$COLUMN_SUMMARY TEXT, " +
                "$COLUMN_TIMESTAMP TEXT)")

        db.execSQL(createUsersTable)
        db.execSQL(createPlantReportsTable)
        db.execSQL(createSoilReportsTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLANT_REPORTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SOIL_REPORTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }
}
