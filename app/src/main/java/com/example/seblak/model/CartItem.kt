package com.example.seblak.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val menu: Menu,
    var quantity: Int = 1
) : Parcelable {
    val subtotal: Double get() = menu.harga * quantity
}