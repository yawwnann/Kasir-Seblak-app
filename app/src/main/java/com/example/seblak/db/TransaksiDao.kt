package com.example.seblak.db

import android.annotation.SuppressLint // Pastikan import ini ada
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.seblak.model.DetailTransaksiItem
import com.example.seblak.model.Transaksi

open class TransaksiDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val TAG = "TransaksiDao"

    fun simpanTransaksiLengkap(transaksi: Transaksi, detailItems: List<DetailTransaksiItem>): Long {
        // ... (kode simpanTransaksiLengkap yang sudah ada sebelumnya) ...
        // Pastikan kode ini sudah benar dan berfungsi
        val db = dbHelper.writableDatabase
        var idTransaksiBaru: Long = -1L

        db.beginTransaction()
        try {
            val valuesTransaksi = ContentValues().apply {
                put(DatabaseHelper.COLUMN_TRANSAKSI_TANGGAL_WAKTU, transaksi.tanggalWaktu)
                put(DatabaseHelper.COLUMN_TRANSAKSI_TOTAL_HARGA_AKHIR, transaksi.totalHargaAkhir)
                put(DatabaseHelper.COLUMN_TRANSAKSI_METODE_PEMBAYARAN, transaksi.metodePembayaran)
                transaksi.jumlahBayar?.let { put(DatabaseHelper.COLUMN_TRANSAKSI_JUMLAH_BAYAR, it) }
                transaksi.kembalian?.let { put(DatabaseHelper.COLUMN_TRANSAKSI_KEMBALIAN, it) }
            }
            idTransaksiBaru = db.insert(DatabaseHelper.TABLE_TRANSAKSI, null, valuesTransaksi)

            if (idTransaksiBaru == -1L) {
                throw IllegalStateException("Gagal menyimpan header transaksi.")
            }

            detailItems.forEach { detail ->
                val valuesDetail = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_DETAIL_TRANSAKSI_ID_FK, idTransaksiBaru)
                    put(DatabaseHelper.COLUMN_DETAIL_MENU_ID_FK, detail.menuId)
                    put(DatabaseHelper.COLUMN_DETAIL_NAMA_MENU_SNAPSHOT, detail.namaMenuSnapshot)
                    put(DatabaseHelper.COLUMN_DETAIL_HARGA_SATUAN_SNAPSHOT, detail.hargaSatuanSnapshot)
                    put(DatabaseHelper.COLUMN_DETAIL_KUANTITAS, detail.kuantitas)
                    put(DatabaseHelper.COLUMN_DETAIL_SUBTOTAL, detail.subtotal)
                }
                val resultDetail = db.insert(DatabaseHelper.TABLE_DETAIL_TRANSAKSI, null, valuesDetail)
                if (resultDetail == -1L) {
                    throw IllegalStateException("Gagal menyimpan detail transaksi untuk menu ID: ${detail.menuId}")
                }
            }
            db.setTransactionSuccessful()
            Log.d(TAG, "Transaksi berhasil disimpan. ID Transaksi: $idTransaksiBaru, Jumlah Item: ${detailItems.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saat menyimpan transaksi lengkap: ", e)
            idTransaksiBaru = -1L
        } finally {
            db.endTransaction()
        }
        return idTransaksiBaru
    }

    // --- METODE BARU DI BAWAH INI ---

    @SuppressLint("Range")
    open fun getAllTransaksiHeaders(): List<Transaksi> {
        val transaksiList = mutableListOf<Transaksi>()
        val db = dbHelper.readableDatabase
        // Urutkan berdasarkan ID descending agar yang terbaru muncul di atas
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_TRANSAKSI} ORDER BY ${DatabaseHelper.COLUMN_TRANSAKSI_ID} DESC"
        db.rawQuery(query, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    transaksiList.add(
                        Transaksi(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_ID)),
                            tanggalWaktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_TANGGAL_WAKTU)),
                            totalHargaAkhir = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_TOTAL_HARGA_AKHIR)),
                            metodePembayaran = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_METODE_PEMBAYARAN)),
                            jumlahBayar = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_JUMLAH_BAYAR))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_JUMLAH_BAYAR)),
                            kembalian = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_KEMBALIAN))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_KEMBALIAN))
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        return transaksiList
    }

    @SuppressLint("Range")
    fun getTransaksiHeaderById(transaksiId: Long): Transaksi? {
        var transaksi: Transaksi? = null
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_TRANSAKSI} WHERE ${DatabaseHelper.COLUMN_TRANSAKSI_ID} = ?"
        db.rawQuery(query, arrayOf(transaksiId.toString()))?.use { cursor ->
            if (cursor.moveToFirst()) {
                transaksi = Transaksi(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_ID)),
                    tanggalWaktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_TANGGAL_WAKTU)),
                    totalHargaAkhir = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_TOTAL_HARGA_AKHIR)),
                    metodePembayaran = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_METODE_PEMBAYARAN)),
                    jumlahBayar = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_JUMLAH_BAYAR))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_JUMLAH_BAYAR)),
                    kembalian = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_KEMBALIAN))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSAKSI_KEMBALIAN))
                )
            }
        }
        return transaksi
    }

    @SuppressLint("Range")
    fun getDetailItemsByTransaksiId(transaksiId: Long): List<DetailTransaksiItem> {
        val detailList = mutableListOf<DetailTransaksiItem>()
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_DETAIL_TRANSAKSI} WHERE ${DatabaseHelper.COLUMN_DETAIL_TRANSAKSI_ID_FK} = ?"
        db.rawQuery(query, arrayOf(transaksiId.toString()))?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    detailList.add(
                        DetailTransaksiItem(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_ID)),
                            transaksiId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_TRANSAKSI_ID_FK)),
                            menuId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_MENU_ID_FK)),
                            namaMenuSnapshot = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_NAMA_MENU_SNAPSHOT)),
                            hargaSatuanSnapshot = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_HARGA_SATUAN_SNAPSHOT)),
                            kuantitas = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_KUANTITAS)),
                            subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_SUBTOTAL))
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        return detailList
    }
}