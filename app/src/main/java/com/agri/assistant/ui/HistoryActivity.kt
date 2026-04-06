package com.agri.assistant.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.agri.assistant.R

/**
 * History screen is integrated into ReportsActivity via the History tab.
 * This Activity acts as a redirect for backward compatibility.
 */
class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to Reports with history tab
        val intent = android.content.Intent(this, ReportsActivity::class.java)
        intent.putExtra("tab", "history")
        startActivity(intent)
        finish()
    }
}
