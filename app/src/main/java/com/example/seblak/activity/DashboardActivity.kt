package com.example.seblak.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seblak.db.MenuDao
import com.example.seblak.db.TransaksiDao
// import com.example.seblak.db.UserDao // Tidak dipakai langsung di sini lagi
import com.example.seblak.ui.screens.MainDashboardScreen // Import Composable Utama
import com.example.seblak.viewmodel.CartViewModel
import com.example.seblak.ui.screens.LaporanContentScreen
// import com.example.seblak.ui.theme.SeblakTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val menuDao = MenuDao(this)
        val transaksiDao = TransaksiDao(this)
        // UserDao tidak lagi dibuat di sini kecuali ada penggunaan lain

        setContent {
            val cartViewModel: CartViewModel = viewModel()

            // SeblakTheme { // Uncomment jika Anda menggunakan tema utama
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainDashboardScreen( // Memanggil Composable utama dari file lain
                    menuDao = menuDao,
                    transaksiDao = transaksiDao,
                    cartViewModel = cartViewModel,

                    onLogoutClicked = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
            // }
        }
    }
}