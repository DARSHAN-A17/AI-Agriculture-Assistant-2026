package com.agri.assistant.model

data class User(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val location: String,
    val password: String
)

data class PlantReport(
    val id: Long = 0,
    val userId: Long,
    val imagePath: String,
    val plantName: String,
    val diseaseName: String,
    val diseaseDescription: String,
    val cause: String,
    val treatment: String,
    val prevention: String,
    val date: String
)

data class SoilReport(
    val id: Long = 0,
    val userId: Long,
    val imagePath: String = "",
    val soilType: String,
    val nutrientStatus: String,
    val recommendation: String,
    val suitableCrops: String,
    val date: String
)

data class HistoryItem(
    val id: Long = 0,
    val userId: Long,
    val reportType: String, // plant, soil_image, soil_nutrient
    val reportId: Long,
    val summary: String,
    val timestamp: String
)
