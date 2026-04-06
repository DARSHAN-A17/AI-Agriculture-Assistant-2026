package com.agri.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.agri.assistant.R
import com.agri.assistant.db.UserDao
import com.agri.assistant.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userDao = UserDao(this)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etLocation = findViewById<TextInputEditText>(R.id.etLocation)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() || phone.isEmpty() || location.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, getString(R.string.passwords_mismatch), Toast.LENGTH_SHORT).show()
                }
                userDao.isPhoneRegistered(phone) -> {
                    Toast.makeText(this, getString(R.string.phone_exists), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val user = User(name = name, phone = phone, location = location, password = password)
                    val id = userDao.registerUser(user)
                    if (id > 0) {
                        Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.tvLogin).setOnClickListener {
            finish()
        }
    }
}
