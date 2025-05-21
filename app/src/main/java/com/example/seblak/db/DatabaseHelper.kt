package com.example.seblak.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "AplikasiKasir.db"

        // Tabel Menu
        const val TABLE_MENU = "menu"
        const val COLUMN_MENU_ID = "_id" // Standar SQLite untuk primary key
        const val COLUMN_MENU_NAMA = "nama"
        const val COLUMN_MENU_HARGA = "harga"
        const val COLUMN_MENU_DESKRIPSI = "deskripsi"
        const val COLUMN_MENU_IMAGE_URI = "image_uri"

        // Tabel Transaksi
        const val TABLE_TRANSAKSI = "transaksi"
        const val COLUMN_TRANSAKSI_ID = "_id"
        const val COLUMN_TRANSAKSI_TANGGAL = "tanggal"
        const val COLUMN_TRANSAKSI_TOTAL_HARGA = "total_harga"

        // Tabel Detail Transaksi (untuk mencatat item per transaksi)
        const val TABLE_DETAIL_TRANSAKSI = "detail_transaksi"
        const val COLUMN_DETAIL_ID = "_id"
        const val COLUMN_DETAIL_TRANSAKSI_ID = "transaksi_id" // Foreign Key
        const val COLUMN_DETAIL_MENU_ID = "menu_id"           // Foreign Key
        const val COLUMN_DETAIL_NAMA_MENU = "nama_menu"
        const val COLUMN_DETAIL_JUMLAH = "jumlah"
        const val COLUMN_DETAIL_HARGA_SATUAN = "harga_satuan"
        const val COLUMN_DETAIL_SUBTOTAL = "subtotal"

        // Tabel User
        const val TABLE_USER = "users" // Nama tabel bisa 'user' atau 'users'
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_USER_USERNAME = "username"
        const val COLUMN_USER_PASSWORD_HASH = "password_hash"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE_USER = ("CREATE TABLE $TABLE_USER ("
                + "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USER_USERNAME TEXT UNIQUE NOT NULL," // Username harus unik
                + "$COLUMN_USER_PASSWORD_HASH TEXT NOT NULL" + ")")
        db?.execSQL(CREATE_TABLE_USER)

        val CREATE_TABLE_MENU = ("CREATE TABLE $TABLE_MENU ("
                + "$COLUMN_MENU_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_MENU_NAMA TEXT NOT NULL,"
                + "$COLUMN_MENU_HARGA REAL NOT NULL,"
                + "$COLUMN_MENU_DESKRIPSI TEXT,"
                + "$COLUMN_MENU_IMAGE_URI TEXT" + ")")
        db?.execSQL(CREATE_TABLE_MENU)

        val CREATE_TABLE_TRANSAKSI = ("CREATE TABLE $TABLE_TRANSAKSI ("
                + "$COLUMN_TRANSAKSI_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TRANSAKSI_TANGGAL TEXT NOT NULL," // Simpan sebagai TEXT (ISO8601) atau INTEGER (timestamp Unix)
                + "$COLUMN_TRANSAKSI_TOTAL_HARGA REAL NOT NULL" + ")")
        db?.execSQL(CREATE_TABLE_TRANSAKSI)

        val CREATE_TABLE_DETAIL_TRANSAKSI = ("CREATE TABLE $TABLE_DETAIL_TRANSAKSI ("
                + "$COLUMN_DETAIL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_DETAIL_TRANSAKSI_ID INTEGER NOT NULL,"
                + "$COLUMN_DETAIL_MENU_ID INTEGER NOT NULL,"
                + "$COLUMN_DETAIL_NAMA_MENU TEXT NOT NULL,"
                + "$COLUMN_DETAIL_JUMLAH INTEGER NOT NULL,"
                + "$COLUMN_DETAIL_HARGA_SATUAN REAL NOT NULL,"
                + "$COLUMN_DETAIL_SUBTOTAL REAL NOT NULL,"
                + "FOREIGN KEY($COLUMN_DETAIL_TRANSAKSI_ID) REFERENCES $TABLE_TRANSAKSI($COLUMN_TRANSAKSI_ID) ON DELETE CASCADE,"
                + "FOREIGN KEY($COLUMN_DETAIL_MENU_ID) REFERENCES $TABLE_MENU($COLUMN_MENU_ID)" + ")")
        db?.execSQL(CREATE_TABLE_DETAIL_TRANSAKSI)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_MENU ADD COLUMN $COLUMN_MENU_IMAGE_URI TEXT")
        }

        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DETAIL_TRANSAKSI")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MENU")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true) // Aktifkan foreign key constraint
    }
}