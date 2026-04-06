package com.agri.assistant.db

import android.content.ContentValues
import android.content.Context
import com.agri.assistant.model.User

class UserDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun registerUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, user.name)
            put(DatabaseHelper.COLUMN_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_LOCATION, user.location)
            put(DatabaseHelper.COLUMN_PASSWORD, user.password)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }

    fun loginUser(phone: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS, null,
            "${DatabaseHelper.COLUMN_PHONE}=? AND ${DatabaseHelper.COLUMN_PASSWORD}=?",
            arrayOf(phone, password), null, null, null
        )

        var user: User? = null
        if (cursor != null && cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION)),
                password = "" // Don't return password
            )
            cursor.close()
        }
        return user
    }

    fun isPhoneRegistered(phone: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS, arrayOf(DatabaseHelper.COLUMN_ID),
            "${DatabaseHelper.COLUMN_PHONE}=?", arrayOf(phone),
            null, null, null
        )
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }

    fun updateUser(user: User): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, user.name)
            put(DatabaseHelper.COLUMN_LOCATION, user.location)
        }
        return db.update(DatabaseHelper.TABLE_USERS, values, "${DatabaseHelper.COLUMN_ID}=?", arrayOf(user.id.toString()))
    }
}
