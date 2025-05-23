package com.example.seblak.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.activity.TambahMenuActivity
import com.example.seblak.db.MenuDao
import com.example.seblak.db.TransaksiDao
import com.example.seblak.model.BottomNavItem
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
import com.example.seblak.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    menuDao: MenuDao,
    transaksiDao: TransaksiDao,
    cartViewModel: CartViewModel,
    onLogoutClicked: () -> Unit // Fungsi ini akan dipanggil setelah konfirmasi
) {
    var currentScreen by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Dasbor) }
    val navItems = listOf(
        BottomNavItem.Dasbor,
        BottomNavItem.Menu,
        BottomNavItem.Kasir,
        BottomNavItem.Laporan,
        BottomNavItem.Logout
    )
    val context = LocalContext.current
    val cartItemCount by cartViewModel.cartItemCount.collectAsState()

    // State untuk dialog konfirmasi logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClicked() // Panggil aksi logout sebenarnya
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
                ) {
                    Text("Ya, Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onItemSelected = { selectedScreen ->
                    if (selectedScreen == BottomNavItem.Logout) {
                        showLogoutDialog = true // Tampilkan dialog, bukan langsung logout
                    } else {
                        currentScreen = selectedScreen
                    }
                },
                navItems = navItems,
                cartItemCount = cartItemCount
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                BottomNavItem.Dasbor -> DasborContentScreen(
                    onNavigateToTambahMenu = {
                        context.startActivity(Intent(context, TambahMenuActivity::class.java))
                    },
                    onNavigateToMenuTab = { currentScreen = BottomNavItem.Menu },
                    onNavigateToKasirTab = { currentScreen = BottomNavItem.Kasir },
                    onNavigateToLaporanTab = { currentScreen = BottomNavItem.Laporan }
                )
                BottomNavItem.Menu -> MenuContentScreen(
                    menuDao = menuDao,
                    cartViewModel = cartViewModel,
                    onNavigateToTambahMenu = {
                        context.startActivity(Intent(context, TambahMenuActivity::class.java))
                    }
                )
                BottomNavItem.Kasir -> KasirContentScreen(cartViewModel = cartViewModel)
                BottomNavItem.Laporan -> LaporanContentScreen(transaksiDao = transaksiDao)
                BottomNavItem.Logout -> {
                    // Konten ini tidak akan tampil lama karena dialog akan muncul
                    // atau navigasi sudah terjadi.
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomNavigationBar(
    currentScreen: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    navItems: List<BottomNavItem>,
    cartItemCount: Int
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        contentColor = PrimaryRedText, // Warna default ikon tidak aktif
        tonalElevation = 0.dp
    ) {
        navItems.forEach { screen ->
            val isSelected = currentScreen.route == screen.route && screen != BottomNavItem.Logout

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(screen) },
                icon = {
                    val iconToShow = screen.icon
                    val titleToShow = screen.title // Tetap perlukan untuk content description

                    if (isSelected) {
                        // Item terpilih: Hanya ikon di dalam pil
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(ButtonRed)
                                .padding(horizontal = 16.dp, vertical = 10.dp), // Padding untuk pil
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                iconToShow,
                                contentDescription = titleToShow,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp) // Ukuran ikon di dalam pil
                            )
                        }
                    } else {
                        // Item tidak terpilih: Ikon saja (label akan ditangani oleh slot `label`)
                        // atau ikon dengan badge untuk Kasir
                        if (screen is BottomNavItem.Kasir && cartItemCount > 0) {
                            BadgedBox(badge = { Badge { Text("$cartItemCount") } }) {
                                Icon(
                                    iconToShow,
                                    contentDescription = titleToShow,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                iconToShow,
                                contentDescription = titleToShow,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                label = if (!isSelected) { // Tampilkan label HANYA jika tidak terpilih
                    { Text(screen.title, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else null, // Tidak ada label jika terpilih (karena ikon sudah di dalam pil)
                alwaysShowLabel = false, // Ini akan membuat label item tidak terpilih muncul jika ada ruang
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent, // Indikator default tidak digunakan
                    selectedIconColor = Color.White, // Dihandle manual di dalam Box pil
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // selectedTextColor tidak relevan jika label null saat terpilih
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}