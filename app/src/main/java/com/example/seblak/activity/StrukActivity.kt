package com.example.seblak.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.db.TransaksiDao
import com.example.seblak.model.DetailTransaksiItem
import com.example.seblak.model.Menu // Untuk Preview
import com.example.seblak.model.Transaksi
import com.example.seblak.ui.common.DecorativeRedBackground // Pastikan ini diimpor
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
// import com.example.seblak.ui.theme.SeblakTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StrukActivity : ComponentActivity() {
    private lateinit var transaksiDao: TransaksiDao
    private val TAG = "StrukActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transaksiDao = TransaksiDao(this)

        val transactionId = intent.getLongExtra(CheckoutActivity.EXTRA_STRUK_TRANSACTION_ID, -1L)
        val fallbackItemsStrings = intent.getStringArrayListExtra(CheckoutActivity.EXTRA_STRUK_ITEMS_DETAILS_STRINGS)
        val fallbackTotal = intent.getDoubleExtra(CheckoutActivity.EXTRA_STRUK_TOTAL, 0.0)
        val fallbackMetode = intent.getStringExtra(CheckoutActivity.EXTRA_STRUK_METODE)
        val fallbackBayar = intent.getDoubleExtra(CheckoutActivity.EXTRA_STRUK_BAYAR, 0.0)
        val fallbackKembalian = intent.getDoubleExtra(CheckoutActivity.EXTRA_STRUK_KEMBALIAN, 0.0)
        val fallbackTanggalWaktu = intent.getStringExtra(CheckoutActivity.EXTRA_STRUK_TANGGAL_WAKTU)

        setContent {
            // SeblakTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                var transaksiState by remember { mutableStateOf<Transaksi?>(null) }
                var detailItemsState by remember { mutableStateOf<List<DetailTransaksiItem>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                var errorLoading by remember { mutableStateOf(false) }
                var useFallbackData by remember { mutableStateOf(false) }

                LaunchedEffect(transactionId) {
                    if (transactionId != -1L) {
                        isLoading = true
                        errorLoading = false
                        Log.d(TAG, "Fetching transaction details for ID: $transactionId")
                        try {
                            withContext(Dispatchers.IO) {
                                transaksiState = transaksiDao.getTransaksiHeaderById(transactionId)
                                if (transaksiState != null) {
                                    detailItemsState = transaksiDao.getDetailItemsByTransaksiId(transactionId)
                                    Log.d(TAG, "Transaction header found: ${transaksiState?.id}, Details count: ${detailItemsState.size}")
                                } else {
                                    Log.e(TAG, "Transaction header NOT FOUND for ID: $transactionId")
                                    errorLoading = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching transaction details", e)
                            errorLoading = true
                        } finally {
                            isLoading = false
                        }
                    } else if (fallbackItemsStrings != null && fallbackMetode != null && fallbackTanggalWaktu != null) {
                        Log.w(TAG, "Transaction ID not provided, using fallback data from Intent.")
                        useFallbackData = true
                        isLoading = false
                    } else {
                        Log.e(TAG, "Transaction ID is invalid (-1) and no fallback data provided.")
                        errorLoading = true
                        isLoading = false
                    }
                }

                StrukScreenContent(
                    isLoading = isLoading,
                    errorLoading = errorLoading,
                    transaksi = if (useFallbackData) null else transaksiState,
                    detailItems = if (useFallbackData) null else detailItemsState,
                    fallbackItemsStrings = if(useFallbackData) fallbackItemsStrings else null,
                    fallbackTotal = if(useFallbackData) fallbackTotal else null,
                    fallbackMetode = if(useFallbackData) fallbackMetode else null,
                    fallbackBayar = if(useFallbackData) fallbackBayar else null,
                    fallbackKembalian = if(useFallbackData) fallbackKembalian else null,
                    fallbackTanggalWaktu = if(useFallbackData) fallbackTanggalWaktu else null,
                    onSelesaiClicked = {
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
            // }
        }
    }
}
@Composable
fun DecorativeRedBackground(modifier: Modifier = Modifier, alpha: Float = 0.2f) { // Tambahkan parameter alpha dengan nilai default
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-90).dp, y = (-120).dp)
                .size(280.dp)
                .background(
                    LightRedBackground.copy(alpha = alpha),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (80).dp, y = (100).dp)
                .size(320.dp)
                .background(
                    MediumRedBackground.copy(alpha = alpha),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (60).dp, y = (180).dp)
                .size(130.dp)
                .rotate(25f)
                .background(
                    AccentRedBackground.copy(alpha = alpha),
                    RoundedCornerShape(35.dp)
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrukScreenContent(
    isLoading: Boolean,
    errorLoading: Boolean,
    transaksi: Transaksi?,
    detailItems: List<DetailTransaksiItem>?,
    fallbackItemsStrings: List<String>?,
    fallbackTotal: Double?,
    fallbackMetode: String?,
    fallbackBayar: Double?,
    fallbackKembalian: Double?,
    fallbackTanggalWaktu: String?,
    onSelesaiClicked: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    Box(modifier = Modifier.fillMaxSize()){ // Box utama untuk background
        DecorativeRedBackground(alpha = 0.1f) // Background dekoratif dengan alpha lebih rendah

        Scaffold(
            containerColor = Color.Transparent, // Membuat Scaffold transparan agar background terlihat
            bottomBar = {
                Button(
                    onClick = onSelesaiClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp) // Padding lebih besar
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp), // Bentuk tombol disamakan
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
                ) {
                    Text("Selesai & Kembali", fontSize = 16.sp, color = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding dari Scaffold (untuk bottomBar)
                    .padding(horizontal = 20.dp, vertical = 16.dp) // Padding konten utama
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator(color = PrimaryRedText, modifier = Modifier.padding(32.dp))
                    }
                } else if (errorLoading || (transaksi == null && fallbackTanggalWaktu == null)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text("Gagal memuat data struk atau transaksi tidak ditemukan.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    val displayTanggalWaktu = transaksi?.tanggalWaktu?.let {
                        try {
                            val dbSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val displaySdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("in", "ID"))
                            displaySdf.format(dbSdf.parse(it) ?: Date())
                        } catch (e: Exception) { it }
                    } ?: fallbackTanggalWaktu ?: "-"

                    val displayTotalHarga = transaksi?.totalHargaAkhir ?: fallbackTotal ?: 0.0
                    val displayMetodeBayar = transaksi?.metodePembayaran ?: fallbackMetode ?: "N/A"
                    val displayBayar = transaksi?.jumlahBayar ?: fallbackBayar ?: 0.0
                    val displayKembalian = transaksi?.kembalian ?: fallbackKembalian ?: 0.0
                    val displayTransactionId = transaksi?.id

                    // --- Konten Struk ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp) // Jarak sebelum tombol Selesai
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CheckCircle, "Pembayaran Sukses", tint = Color(0xFF4CAF50), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pembayaran Berhasil!", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF388E3C)))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(displayTanggalWaktu, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (displayTransactionId != null && displayTransactionId != 0L) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("No. Transaksi: #$displayTransactionId", style = MaterialTheme.typography.bodyMedium, color = PrimaryRedText, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Text("Seblak Titik Koma", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = PrimaryRedText), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("Jl. Kenangan Indah No. 45, Yogyakarta", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

                        Text("--------------------------------------", fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Detail Pesanan:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryRedText)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (detailItems != null && detailItems.isNotEmpty()) {
                            detailItems.forEach { item ->
                                StrukItemRow(
                                    nama = item.namaMenuSnapshot,
                                    kuantitas = item.kuantitas,
                                    hargaSatuan = item.hargaSatuanSnapshot,
                                    subtotal = item.subtotal,
                                    currencyFormatter = currencyFormatter
                                )
                            }
                        } else if (fallbackItemsStrings != null && fallbackItemsStrings.isNotEmpty()) {
                            fallbackItemsStrings.forEach { itemString ->
                                Text(itemString, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 20.sp)) // Line height lebih besar
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        } else {
                            Text("Tidak ada detail item.", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("--------------------------------------", fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))

                        StrukSummaryRow("Total Harga:", currencyFormatter.format(displayTotalHarga), isTotal = true)
                        StrukSummaryRow("Metode Bayar:", displayMetodeBayar)
                        if (displayMetodeBayar.equals("Tunai", ignoreCase = true) || displayMetodeBayar.equals("CASH", ignoreCase = true)) {
                            StrukSummaryRow("Uang Bayar:", currencyFormatter.format(displayBayar))
                            StrukSummaryRow("Kembalian:", currencyFormatter.format(displayKembalian))
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Text("Terima Kasih!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryRedText), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("Follow kami @seblaktitikkoma", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top=4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StrukItemRow(nama: String, kuantitas: Int, hargaSatuan: Double, subtotal: Double, currencyFormatter: NumberFormat) {
    Column {
        Text(
            text = nama,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "  ${kuantitas}x ${currencyFormatter.format(hargaSatuan)} = ${currencyFormatter.format(subtotal)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}


@Composable
fun StrukSummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            else MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = if (isTotal) PrimaryRedText else MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = if (isTotal) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            else MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = if (isTotal) PrimaryRedText else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
    Spacer(modifier = Modifier.height(if (isTotal) 8.dp else 6.dp))
}


@Preview(showBackground = true)
@Composable
fun StrukScreenPreviewCash() {
    MaterialTheme {
        StrukScreenContent(
            isLoading = false, errorLoading = false,
            transaksi = Transaksi(id=123, tanggalWaktu="2025-05-23 10:30:00", totalHargaAkhir=29000.0, metodePembayaran="Tunai", jumlahBayar=30000.0, kembalian=1000.0),
            detailItems = listOf(
                DetailTransaksiItem(transaksiId=123, menuId=1, namaMenuSnapshot="Seblak Ori Pedas Nikmat", hargaSatuanSnapshot=12000.0, kuantitas=2, subtotal=24000.0),
                DetailTransaksiItem(transaksiId=123, menuId=2, namaMenuSnapshot="Es Teh Manis Segar", hargaSatuanSnapshot=5000.0, kuantitas=1, subtotal=5000.0)
            ),
            fallbackItemsStrings = null, fallbackTotal = null, fallbackMetode = null, fallbackBayar = null, fallbackKembalian = null, fallbackTanggalWaktu = null,
            onSelesaiClicked = {}
        )
    }
}