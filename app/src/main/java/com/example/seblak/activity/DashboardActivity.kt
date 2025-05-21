package com.example.seblak.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.db.MenuDao
import com.example.seblak.db.UserDao
import com.example.seblak.ui.theme.PrimaryRedText
import com.example.seblak.ui.theme.ButtonRed
import androidx.compose.material.icons.automirrored.filled.Logout

// Import untuk MenuContentScreen dari package yang benar
import com.example.seblak.ui.screens.MenuContentScreen // Pastikan path package ini benar

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dasbor : BottomNavItem("dasbor", "Dasbor", Icons.Filled.Home)
    object Menu : BottomNavItem("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Kasir : BottomNavItem("kasir", "Kasir", Icons.Filled.PointOfSale)
    object Laporan : BottomNavItem("laporan", "Laporan", Icons.Filled.Assessment)
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi DAO
        // val userDao = UserDao(this) // Tidak digunakan di MainDashboardScreen saat ini, bisa dihapus jika tidak dipakai di TopAppBar
        val menuDao = MenuDao(this)
        setContent {
            // Jika Anda menggunakan tema kustom, uncomment dan gunakan di sini
            // SeblakTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainDashboardScreen(
                    menuDao = menuDao,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(menuDao: MenuDao, onLogoutClicked: () -> Unit) {
    var currentScreen by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Dasbor) }
    val navItems = listOf(BottomNavItem.Dasbor, BottomNavItem.Menu, BottomNavItem.Kasir, BottomNavItem.Laporan)
    val context = LocalContext.current

    Scaffold(

        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onItemSelected = { selectedScreen ->
                    currentScreen = selectedScreen
                },
                navItems = navItems
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                BottomNavItem.Dasbor -> DasborContentScreen()
                BottomNavItem.Menu -> MenuContentScreen(
                    menuDao = menuDao,
                    onNavigateToTambahMenu = {
                        context.startActivity(Intent(context, TambahMenuActivity::class.java))
                    }
                )
                BottomNavItem.Kasir -> KasirContentScreen()
                BottomNavItem.Laporan -> LaporanContentScreen()
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    navItems: List<BottomNavItem>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        contentColor = PrimaryRedText,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { screen ->
            val isSelected = currentScreen.route == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(screen) },
                icon = {
                    if (isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(ButtonRed)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                screen.title,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    } else {
                        Icon(
                            screen.icon,
                            contentDescription = screen.title,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                label = if (!isSelected) { { Text(screen.title, fontSize = 11.sp, maxLines = 1) } } else null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color.White,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = Color.White,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun DasborContentScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Halaman Dasbor", style = MaterialTheme.typography.headlineMedium, color = PrimaryRedText)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            context.startActivity(Intent(context, TambahMenuActivity::class.java))
        }) {
            Text("Tambah Menu Baru (Direct)")
        }
        Button(onClick = { /* TODO: Implementasi Fitur Dasbor 1 */ }) { Text("Fitur Dasbor 1") }
        Button(onClick = { /* TODO: Implementasi Fitur Dasbor 2 */ }) { Text("Fitur Dasbor 2") }
    }
}

@Composable
fun KasirContentScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Halaman Kasir (Transaksi)", style = MaterialTheme.typography.headlineMedium, color = PrimaryRedText)
    }
}

@Composable
fun LaporanContentScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Halaman Laporan Transaksi", style = MaterialTheme.typography.headlineMedium, color = PrimaryRedText)
    }
}

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    MaterialTheme { // Tambahkan MaterialTheme untuk preview yang lebih baik
        MainDashboardScreen(
            menuDao = MenuDao(LocalContext.current), // Perlu MenuDao untuk preview
            onLogoutClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    MaterialTheme { // Tambahkan MaterialTheme
        val navItems = listOf(BottomNavItem.Dasbor, BottomNavItem.Menu, BottomNavItem.Kasir, BottomNavItem.Laporan)
        AppBottomNavigationBar(currentScreen = BottomNavItem.Dasbor, onItemSelected = {}, navItems = navItems)
    }
}

@Preview(showBackground = true)
@Composable
fun DasborContentScreenPreview() {
    MaterialTheme { // Tambahkan MaterialTheme
        DasborContentScreen()
    }
}