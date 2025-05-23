package com.example.seblak.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seblak.activity.TambahMenuActivity
import com.example.seblak.model.BottomNavItem // Pastikan ini diimpor jika Anda menggunakannya untuk navigasi tab
import com.example.seblak.ui.common.DecorativeRedBackground // Pastikan ini diimpor jika Anda menggunakannya
import com.example.seblak.ui.theme.PrimaryRedText
// import com.example.seblak.ui.theme.SeblakTheme

// Data class untuk item navigasi di dasbor
data class DashboardGridItem(
    val title: String,
    val icon: ImageVector,
    val onClickAction: () -> Unit
)

@Composable
fun DasborContentScreen(
    onNavigateToTambahMenu: () -> Unit,
    onNavigateToMenuTab: () -> Unit,
    onNavigateToKasirTab: () -> Unit,
    onNavigateToLaporanTab: () -> Unit
) {
    val context = LocalContext.current

    val dashboardGridItems = listOf(
        DashboardGridItem("Tambah Menu", Icons.Filled.AddBusiness, onNavigateToTambahMenu),
        DashboardGridItem("Daftar Menu", Icons.Filled.MenuBook, onNavigateToMenuTab),
        DashboardGridItem("Buat Transaksi", Icons.Filled.PointOfSale, onNavigateToKasirTab),
        DashboardGridItem("Lihat Laporan", Icons.Filled.Assessment, onNavigateToLaporanTab)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeRedBackground() // Latar belakang dekoratif

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Selamat Datang di Kasir Seblak!",
                style = MaterialTheme.typography.headlineSmall, // Sedikit lebih kecil dari headlineMedium
                color = PrimaryRedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Dua kartu per baris
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dashboardGridItems) { item ->
                    DashboardNavigationCard(
                        title = item.title,
                        icon = item.icon,
                        onClick = item.onClickAction
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Diperlukan untuk Card
@Composable
fun DashboardNavigationCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // Rasio aspek kartu
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = PrimaryRedText
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DasborContentScreenPreview() {
    // SeblakTheme { // Aktifkan jika Anda memiliki tema utama
    MaterialTheme { // Menggunakan MaterialTheme dasar untuk preview jika SeblakTheme tidak di-uncomment
        Surface {
            DasborContentScreen(
                onNavigateToTambahMenu = {},
                onNavigateToMenuTab = {},
                onNavigateToKasirTab = {},
                onNavigateToLaporanTab = {}
            )
        }
    }
    // }
}