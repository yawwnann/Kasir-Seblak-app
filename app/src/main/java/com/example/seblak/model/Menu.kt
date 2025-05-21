package com.example.seblak.model

data class Menu(
    val id: Long = 0, // ID akan di-generate oleh SQLite
    val nama: String,
    val harga: Double,
    val deskripsi: String? = null ,
    val imageUri: String? = null
)