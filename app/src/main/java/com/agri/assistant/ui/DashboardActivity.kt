package com.agri.assistant.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.agri.assistant.R
import com.agri.assistant.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)

        val tvGreeting = findViewById<android.widget.TextView>(R.id.tvGreeting)
        tvGreeting.text = getString(R.string.hello_farmer, session.getUserName())

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardPlant).setOnClickListener {
            startActivity(Intent(this, PlantDetectionActivity::class.java))
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardSoilImage).setOnClickListener {
            startActivity(Intent(this, SoilImageActivity::class.java))
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardSoilNutrient).setOnClickListener {
            startActivity(Intent(this, SoilNutrientActivity::class.java))
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardReports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
