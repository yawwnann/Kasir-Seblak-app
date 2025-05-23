package com.example.seblak.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
    val id: Long = 0,
    val nama: String,
    val harga: Double,
    val deskripsi: String? = null,
    val imageUri: String? = null
) : Parcelable
