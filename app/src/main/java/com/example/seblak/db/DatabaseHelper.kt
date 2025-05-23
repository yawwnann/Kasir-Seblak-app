package com.example.seblak.db // Pastikan package ini sesuai dengan struktur proyek Anda

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Pertahankan versi 3 jika Anda akan melakukan uninstall dan reinstall.
        // Jika Anda tidak ingin uninstall, naikkan ke 4 agar onUpgrade (yang destruktif) terpanggil.
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "AplikasiKasir_V3_Test.db" // Nama database yang Anda gunakan
        private const val TAG = "DatabaseHelper"

        // Tabel User
        const val TABLE_USER = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_USER_USERNAME = "username"
        const val COLUMN_USER_PASSWORD_HASH = "password_hash"

        // Tabel Menu
        const val TABLE_MENU = "menu"
        const val COLUMN_MENU_ID = "_id"
        const val COLUMN_MENU_NAMA = "nama"
        const val COLUMN_MENU_HARGA = "harga"
        const val COLUMN_MENU_DESKRIPSI = "deskripsi"
        const val COLUMN_MENU_IMAGE_URI = "image_uri"

        // Tabel Transaksi (Header)
        const val TABLE_TRANSAKSI = "transaksi"
        const val COLUMN_TRANSAKSI_ID = "_id"
        const val COLUMN_TRANSAKSI_TANGGAL_WAKTU = "tanggal_waktu"
        const val COLUMN_TRANSAKSI_TOTAL_HARGA_AKHIR = "total_harga_akhir"
        const val COLUMN_TRANSAKSI_METODE_PEMBAYARAN = "metode_pembayaran"
        const val COLUMN_TRANSAKSI_JUMLAH_BAYAR = "jumlah_bayar"
        const val COLUMN_TRANSAKSI_KEMBALIAN = "kembalian"

        // Tabel Detail Transaksi (Item dalam transaksi)
        const val TABLE_DETAIL_TRANSAKSI = "detail_transaksi"
        const val COLUMN_DETAIL_ID = "_id"
        const val COLUMN_DETAIL_TRANSAKSI_ID_FK = "transaksi_id"
        const val COLUMN_DETAIL_MENU_ID_FK = "menu_id"
        const val COLUMN_DETAIL_NAMA_MENU_SNAPSHOT = "nama_menu_snapshot"
        const val COLUMN_DETAIL_HARGA_SATUAN_SNAPSHOT = "harga_satuan_snapshot"
        const val COLUMN_DETAIL_KUANTITAS = "kuantitas"
        const val COLUMN_DETAIL_SUBTOTAL = "subtotal"

        // Perintah SQL untuk membuat tabel
        const val CREATE_TABLE_USER_SQL = ("CREATE TABLE $TABLE_USER ("
                + "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_USER_USERNAME TEXT UNIQUE NOT NULL,"
                + "$COLUMN_USER_PASSWORD_HASH TEXT NOT NULL" + ")")

        const val CREATE_TABLE_MENU_SQL = ("CREATE TABLE $TABLE_MENU ("
                + "$COLUMN_MENU_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_MENU_NAMA TEXT NOT NULL,"
                + "$COLUMN_MENU_HARGA REAL NOT NULL,"
                + "$COLUMN_MENU_DESKRIPSI TEXT,"
                + "$COLUMN_MENU_IMAGE_URI TEXT" + ")")

        const val CREATE_TABLE_TRANSAKSI_SQL = ("CREATE TABLE $TABLE_TRANSAKSI ("
                + "$COLUMN_TRANSAKSI_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TRANSAKSI_TANGGAL_WAKTU TEXT NOT NULL,"
                + "$COLUMN_TRANSAKSI_TOTAL_HARGA_AKHIR REAL NOT NULL,"
                + "$COLUMN_TRANSAKSI_METODE_PEMBAYARAN TEXT NOT NULL,"
                + "$COLUMN_TRANSAKSI_JUMLAH_BAYAR REAL,"
                + "$COLUMN_TRANSAKSI_KEMBALIAN REAL" + ")")

        // --- PERBAIKAN DI SINI ---
        const val CREATE_TABLE_DETAIL_TRANSAKSI_SQL = ("CREATE TABLE $TABLE_DETAIL_TRANSAKSI ("
                + "$COLUMN_DETAIL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_DETAIL_TRANSAKSI_ID_FK INTEGER NOT NULL,"
                + "$COLUMN_DETAIL_MENU_ID_FK INTEGER NOT NULL," // Kolom ini tetap NOT NULL
                + "$COLUMN_DETAIL_NAMA_MENU_SNAPSHOT TEXT NOT NULL,"
                + "$COLUMN_DETAIL_HARGA_SATUAN_SNAPSHOT REAL NOT NULL,"
                + "$COLUMN_DETAIL_KUANTITAS INTEGER NOT NULL,"
                + "$COLUMN_DETAIL_SUBTOTAL REAL NOT NULL,"
                + "FOREIGN KEY($COLUMN_DETAIL_TRANSAKSI_ID_FK) REFERENCES $TABLE_TRANSAKSI($COLUMN_TRANSAKSI_ID) ON DELETE CASCADE,"
                // Mengubah ON DELETE SET NULL menjadi ON DELETE RESTRICT
                + "FOREIGN KEY($COLUMN_DETAIL_MENU_ID_FK) REFERENCES $TABLE_MENU($COLUMN_MENU_ID) ON DELETE RESTRICT"
                + ")")

    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.i(TAG, "onCreate: Creating all tables for new database. Name: $DATABASE_NAME, Version: $DATABASE_VERSION")
        db?.execSQL(CREATE_TABLE_USER_SQL)
        db?.execSQL(CREATE_TABLE_MENU_SQL)
        db?.execSQL(CREATE_TABLE_TRANSAKSI_SQL)
        db?.execSQL(CREATE_TABLE_DETAIL_TRANSAKSI_SQL) // Akan menggunakan definisi SQL yang sudah diperbaiki
        Log.i(TAG, "onCreate: All tables created successfully.")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // onUpgrade Anda yang sekarang bersifat destruktif.
        // Ini akan memastikan bahwa jika versi naik, semua tabel lama dihapus
        // dan onCreate dipanggil untuk membuat tabel dengan skema terbaru.
        // Ini juga akan menerapkan perbaikan pada CREATE_TABLE_DETAIL_TRANSAKSI_SQL
        // jika Anda menaikkan DATABASE_VERSION di masa depan.
        Log.w(TAG, "onUpgrade: Dropping all tables and recreating. OLD_VERSION=$oldVersion, NEW_VERSION=$newVersion. Name: $DATABASE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DETAIL_TRANSAKSI")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MENU")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true) // Aktifkan foreign key constraint
        Log.d(TAG, "onConfigure: Foreign key constraints enabled.")
    }
}