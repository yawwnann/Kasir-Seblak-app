package com.example.seblak.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailTransaksiItem(
    val id: Long = 0,
    var transaksiId: Long = 0,
    val menuId: Long,
    val namaMenuSnapshot: String,
    val hargaSatuanSnapshot: Double,
    val kuantitas: Int,
    val subtotal: Double
) : Parcelable