package com.example.seblak.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.activity.CheckoutActivity // Untuk konstanta EXTRA
import com.example.seblak.activity.StrukActivity
import com.example.seblak.db.TransaksiDao
import com.example.seblak.model.Transaksi
import com.example.seblak.ui.theme.PrimaryRedText
// import com.example.seblak.ui.theme.SeblakTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LaporanContentScreen(transaksiDao: TransaksiDao) {
    var historiTransaksi by remember { mutableStateOf<List<Transaksi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { // Hanya dijalankan sekali saat pertama kali masuk
        isLoading = true
        errorLoading = false
        try {
            historiTransaksi = withContext(Dispatchers.IO) {
                transaksiDao.getAllTransaksiHeaders()
            }
        } catch (e: Exception) {
            errorLoading = true
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Riwayat Transaksi",
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryRedText,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorLoading -> {
                Text(
                    "Gagal memuat riwayat transaksi.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
            }
            historiTransaksi.isEmpty() -> {
                Text(
                    "Belum ada riwayat transaksi.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = historiTransaksi, key = { trx -> trx.id }) { transaksi ->
                        TransaksiHistoryItemCard(
                            transaksi = transaksi,
                            onClick = {
                                val intent = Intent(context, StrukActivity::class.java).apply {
                                    putExtra(CheckoutActivity.EXTRA_STRUK_TRANSACTION_ID, transaksi.id)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransaksiHistoryItemCard(transaksi: Transaksi, onClick: () -> Unit) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    val displayDateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")) }
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    val formattedDate = try {
        val dateObj = dbDateFormat.parse(transaksi.tanggalWaktu)
        if (dateObj != null) displayDateFormat.format(dateObj) else transaksi.tanggalWaktu
    } catch (e: Exception) {
        transaksi.tanggalWaktu // Tampilkan apa adanya jika format parse gagal
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ID: #${transaksi.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Total: ${currencyFormatter.format(transaksi.totalHargaAkhir)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryRedText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Metode: ${transaksi.metodePembayaran}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LaporanContentScreenPreview() {
    val context = LocalContext.current
    val dummyDao = object : TransaksiDao(context) {
        override fun getAllTransaksiHeaders(): List<Transaksi> {
            return listOf(
                Transaksi(1, "2025-05-23 10:30:00", 29000.0, "Tunai", 30000.0, 1000.0),
                Transaksi(2, "2025-05-23 11:00:00", 15000.0, "QRIS", 15000.0, 0.0)
            )
        }
    }
    MaterialTheme {
        Surface {
            LaporanContentScreen(transaksiDao = dummyDao)
        }
    }
}