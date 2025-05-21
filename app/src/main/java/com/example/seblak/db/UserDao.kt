package com.example.seblak.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.example.seblak.model.User
import java.security.MessageDigest

class UserDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * PENTING: Fungsi hashPassword ini adalah contoh SANGAT SEDERHANA untuk tujuan demonstrasi.
     * JANGAN GUNAKAN INI DI APLIKASI PRODUKSI.
     * Gunakan library hashing yang kuat dan aman seperti bcrypt atau Argon2.
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256") // Atau algoritma hash lain
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun registerUser(username: String, plainTextPassword: String): Long {
        // Cek apakah username sudah ada
        if (getUserByUsername(username) != null) {
            return -2L // Indikasi username sudah terdaftar
        }

        val db = dbHelper.writableDatabase
        val hashedPassword = hashPassword(plainTextPassword)
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_USERNAME, username)
            put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, hashedPassword)
        }
        val id = db.insert(DatabaseHelper.TABLE_USER, null, values)
//        db.close()
        return id // Mengembalikan ID baris baru, -1 jika error, -2 jika username sudah ada
    }

    @SuppressLint("Range")
    fun getUserByUsername(username: String): User? {
        val db = dbHelper.readableDatabase
        var user: User? = null
        // Gunakan try-with-resources untuk cursor
        db.query(
            DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USER_USERNAME, DatabaseHelper.COLUMN_USER_PASSWORD_HASH),
            "${DatabaseHelper.COLUMN_USER_USERNAME} = ?",
            arrayOf(username),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                user = User(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_USERNAME)),
                    passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD_HASH))
                )
            }
        }
//        db.close() // Jangan lupa tutup database jika tidak menggunakan instance singleton dari dbHelper
        return user
    }

    fun verifyLogin(username: String, plainTextPassword: String): Boolean {
        val user = getUserByUsername(username)
        return if (user != null) {
            val hashedAttemptPassword = hashPassword(plainTextPassword)
            user.passwordHash == hashedAttemptPassword
        } else {
            false
        }
    }
}