package com.example.seblak.model // Atau package com.example.seblak.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout // Menggunakan versi AutoMirrored
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dasbor : BottomNavItem("dasbor", "Dasbor", Icons.Filled.Home)
    object Menu : BottomNavItem("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Kasir : BottomNavItem("kasir", "Kasir", Icons.Filled.ShoppingCart)
    object Laporan : BottomNavItem("laporan", "Laporan", Icons.Filled.Assessment)
    object Logout : BottomNavItem("logout", "Logout", Icons.AutoMirrored.Filled.Logout)
}