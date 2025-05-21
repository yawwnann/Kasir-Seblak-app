package com.example.seblak.model

data class Transaksi(
    val id: Long = 0,
    val tanggal: String,
    val totalHarga: Double,
    val items: List<DetailTransaksiItem> // Untuk menyimpan detail item yang dibeli
)

data class DetailTransaksiItem(
    val id: Long = 0,
    val transaksiId: Long, // Foreign key ke tabel transaksi utama
    val menuId: Long,      // Foreign key ke tabel menu
    val namaMenu: String,  // Denormalisasi untuk kemudahan display
    val jumlah: Int,
    val hargaSatuan: Double,
    val subtotal: Double
)