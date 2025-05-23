package com.example.seblak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.seblak.model.CartItem
import com.example.seblak.model.Menu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // StateFlow untuk total harga keranjang
    val totalCartPrice: StateFlow<Double> = cartItems
        .map { list ->
            list.sumOf { it.subtotal }
        }
        .stateIn(
            scope = viewModelScope, // Scope dari ViewModel
            started = SharingStarted.WhileSubscribed(5000L), // Aktif selama ada subscriber + 5 detik
            initialValue = 0.0
        )

    // StateFlow untuk jumlah total item di keranjang (memperhitungkan kuantitas tiap item)
    val cartItemCount: StateFlow<Int> = cartItems
        .map { list ->
            list.sumOf { it.quantity }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    fun addMenuToCart(menu: Menu) {
        _cartItems.update { currentList ->
            val existingItem = currentList.find { it.menu.id == menu.id }
            if (existingItem != null) {
                currentList.map {
                    if (it.menu.id == menu.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            } else {
                // Jika item baru, tambahkan ke daftar
                currentList + CartItem(menu = menu, quantity = 1)
            }
        }
    }

    fun incrementItemQuantity(menuId: Long) {
        _cartItems.update { currentList ->
            currentList.map {
                if (it.menu.id == menuId) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        }
    }

    fun decrementItemQuantity(menuId: Long) {
        _cartItems.update { currentList ->
            val itemToDecrement = currentList.find { it.menu.id == menuId }
            if (itemToDecrement != null) {
                if (itemToDecrement.quantity > 1) {
                    // Kurangi kuantitas jika lebih dari 1
                    currentList.map {
                        if (it.menu.id == menuId) {
                            it.copy(quantity = it.quantity - 1)
                        } else {
                            it
                        }
                    }
                } else {
                    // Hapus item jika kuantitasnya 1 dan akan dikurangi (menjadi 0)
                    currentList.filterNot { it.menu.id == menuId }
                }
            } else {
                currentList
            }
        }
    }

    fun removeItemFromCart(menuId: Long) {
        _cartItems.update { currentList ->
            currentList.filterNot { it.menu.id == menuId }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}