package com.example.seblak.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Jika Anda ingin meneruskannya via Intent, meskipun untuk penyimpanan DB tidak wajib
data class Transaksi(
    val id: Long = 0, // Akan di-generate oleh SQLite
    val tanggalWaktu: String,
    val totalHargaAkhir: Double,
    val metodePembayaran: String,
    val jumlahBayar: Double?, // Nullable jika metode bukan cash atau bayar pas
    val kembalian: Double?    // Nullable
) : Parcelable