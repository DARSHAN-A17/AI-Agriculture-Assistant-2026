package com.agri.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.agri.assistant.R
import com.agri.assistant.db.UserDao
import com.agri.assistant.model.User
import com.agri.assistant.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        session = SessionManager(this)
        userDao = UserDao(this)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etLocation = findViewById<TextInputEditText>(R.id.etLocation)

        // Pre-fill
        etName.setText(session.getUserName())
        etPhone.setText(session.getUserPhone())
        etLocation.setText(session.getUserLocation())

        setupLanguageSelector()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val location = etLocation.text.toString().trim()

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(
                id = session.getUserId(),
                name = name,
                phone = session.getUserPhone(),
                location = location,
                password = ""
            )
            userDao.updateUser(user)
            session.updateName(name)
            session.updateLocation(location)
            Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    session.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
    }

    private fun setupLanguageSelector() {
        val languages = arrayOf(
            getString(R.string.lang_en),
            getString(R.string.lang_hi),
            getString(R.string.lang_kn),
            getString(R.string.lang_mr),
            getString(R.string.lang_ta),
            getString(R.string.lang_te)
        )
        val codes = arrayOf("en", "hi", "kn", "mr", "ta", "te")

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)
        val actvLanguage = findViewById<AutoCompleteTextView>(R.id.actvLanguage)
        actvLanguage.setAdapter(adapter)

        // Set current selection
        val currentLang = session.getLanguage()
        val index = codes.indexOf(currentLang)
        if (index >= 0) {
            actvLanguage.setText(languages[index], false)
        }

        actvLanguage.setOnItemClickListener { _, _, position, _ ->
            val selectedCode = codes[position]
            if (selectedCode != session.getLanguage()) {
                session.setLanguage(selectedCode)
                // Apply app-wide
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(selectedCode)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
    }
}
