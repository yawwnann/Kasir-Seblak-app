package com.example.seblak.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Tidak digunakan lagi
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.seblak.R
import com.example.seblak.db.TransaksiDao
import com.example.seblak.model.CartItem
import com.example.seblak.model.Menu
import com.example.seblak.model.Transaksi
import com.example.seblak.model.DetailTransaksiItem
import com.example.seblak.ui.common.DecorativeRedBackground
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
// import com.example.seblak.ui.theme.SeblakTheme
import com.example.seblak.viewmodel.CartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.ArrayList

enum class MetodePembayaran {
    NONE, CASH, QRIS
}

class CheckoutActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TOTAL_PRICE = "extra_total_price_checkout"
        const val EXTRA_CART_ITEMS_LIST = "extra_cart_items_list_checkout"
        const val EXTRA_STRUK_ITEMS_DETAILS_STRINGS = "extra_struk_items_details_strings"
        const val EXTRA_STRUK_TOTAL = "extra_struk_total"
        const val EXTRA_STRUK_METODE = "extra_struk_metode"
        const val EXTRA_STRUK_BAYAR = "extra_struk_bayar"
        const val EXTRA_STRUK_KEMBALIAN = "extra_struk_kembalian"
        const val EXTRA_STRUK_TANGGAL_WAKTU = "extra_struk_tanggal_waktu"
        const val EXTRA_STRUK_TRANSACTION_ID = "extra_struk_transaction_id"
        private const val TAG = "CheckoutActivity"
    }

    private lateinit var transaksiDao: TransaksiDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transaksiDao = TransaksiDao(this)

        val totalPriceFromCart = intent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0)
        val cartItemsForCheckout: ArrayList<CartItem> = intent.getParcelableArrayListExtra(EXTRA_CART_ITEMS_LIST) ?: ArrayList()

        Log.d(TAG, "onCreate - Total Price: $totalPriceFromCart, Items Count: ${cartItemsForCheckout.size}")
        if (cartItemsForCheckout.isEmpty()){
            Log.w(TAG, "onCreate - WARNING: cartItemsForCheckout is EMPTY!")
        }

        setContent {
            val cartViewModel: CartViewModel = viewModel()
            val scope = rememberCoroutineScope()

            // SeblakTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CheckoutScreen(
                    totalPrice = totalPriceFromCart,
                    checkoutItems = cartItemsForCheckout,
                    onPaymentConfirmed = { metode, bayar, kembalian ->
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                        val strukItemsDetailsStrings = ArrayList<String>()
                        val dbDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val displayDateFormat = SimpleDateFormat("dd MMMM yy, HH:mm:ss", Locale("in", "ID"))
                        val currentDate = Date()
                        val tanggalWaktuTransaksiDb = dbDateFormat.format(currentDate)
                        val tanggalWaktuStruk = displayDateFormat.format(currentDate)
                        val metodeBayarStr = when (metode) {
                            MetodePembayaran.CASH -> "Tunai"
                            MetodePembayaran.QRIS -> "QRIS"
                            MetodePembayaran.NONE -> "N/A"
                        }

                        val transaksiHeader = Transaksi(
                            tanggalWaktu = tanggalWaktuTransaksiDb,
                            totalHargaAkhir = totalPriceFromCart,
                            metodePembayaran = metodeBayarStr,
                            jumlahBayar = if (metode == MetodePembayaran.CASH) bayar else null,
                            kembalian = if (metode == MetodePembayaran.CASH) kembalian else null
                        )
                        val detailTransaksiList = cartItemsForCheckout.map { cartItem ->
                            strukItemsDetailsStrings.add(
                                "${cartItem.menu.nama} (${cartItem.quantity}x ${currencyFormatter.format(cartItem.menu.harga)}) = ${currencyFormatter.format(cartItem.subtotal)}"
                            )
                            DetailTransaksiItem(
                                menuId = cartItem.menu.id,
                                namaMenuSnapshot = cartItem.menu.nama,
                                hargaSatuanSnapshot = cartItem.menu.harga,
                                kuantitas = cartItem.quantity,
                                subtotal = cartItem.subtotal
                            )
                        }

                        scope.launch {
                            var newTransactionId: Long = -1L
                            withContext(Dispatchers.IO) {
                                newTransactionId = transaksiDao.simpanTransaksiLengkap(transaksiHeader, detailTransaksiList)
                            }
                            if (newTransactionId != -1L) {
                                Toast.makeText(this@CheckoutActivity, "Transaksi ID: $newTransactionId berhasil disimpan", Toast.LENGTH_LONG).show()
                                cartViewModel.clearCart()
                                val strukIntent = Intent(this@CheckoutActivity, StrukActivity::class.java).apply {
                                    putStringArrayListExtra(EXTRA_STRUK_ITEMS_DETAILS_STRINGS, strukItemsDetailsStrings)
                                    putExtra(EXTRA_STRUK_TOTAL, totalPriceFromCart)
                                    putExtra(EXTRA_STRUK_METODE, metodeBayarStr)
                                    if (metode == MetodePembayaran.CASH) {
                                        putExtra(EXTRA_STRUK_BAYAR, bayar)
                                        putExtra(EXTRA_STRUK_KEMBALIAN, kembalian)
                                    }
                                    putExtra(EXTRA_STRUK_TANGGAL_WAKTU, tanggalWaktuStruk)
                                    putExtra(EXTRA_STRUK_TRANSACTION_ID, newTransactionId)
                                }
                                startActivity(strukIntent)
                                finish()
                            } else {
                                Toast.makeText(this@CheckoutActivity, "Gagal menyimpan transaksi.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
            // }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckoutScreen(
    totalPrice: Double,
    checkoutItems: List<CartItem>,
    onPaymentConfirmed: (metode: MetodePembayaran, bayar: Double, kembalian: Double) -> Unit
    // Parameter onBackClicked dihapus dari sini
) {
    var selectedPaymentMethod by remember { mutableStateOf(MetodePembayaran.NONE) }
    var cashPaidAmountString by remember { mutableStateOf("") }
    var changeAmount by remember { mutableStateOf(0.0) }

    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(selectedPaymentMethod) {
        if (selectedPaymentMethod == MetodePembayaran.CASH) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(cashPaidAmountString, totalPrice, selectedPaymentMethod) {
        val paid = cashPaidAmountString.toDoubleOrNull() ?: 0.0
        val calculatedChange = if (paid >= totalPrice) paid - totalPrice else 0.0
        changeAmount = if (selectedPaymentMethod == MetodePembayaran.CASH && paid < totalPrice) {
            0.0
        } else {
            calculatedChange
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeRedBackground(alpha = 0.1f) // Alpha disesuaikan agar lebih subtle

        Scaffold(
            containerColor = Color.Transparent, // Scaffold transparan
            // topBar dihapus
            bottomBar = {
                val canConfirmButtonBeVisible = selectedPaymentMethod != MetodePembayaran.NONE && checkoutItems.isNotEmpty() && totalPrice > 0.0
                if (canConfirmButtonBeVisible) {
                    Button(
                        onClick = {
                            val paidAmount = if (selectedPaymentMethod == MetodePembayaran.CASH) {
                                cashPaidAmountString.toDoubleOrNull() ?: 0.0
                            } else {
                                totalPrice
                            }
                            if (selectedPaymentMethod == MetodePembayaran.CASH && paidAmount < totalPrice) {
                                Toast.makeText(context, "Uang yang dibayarkan kurang!", Toast.LENGTH_SHORT).show()
                            } else {
                                onPaymentConfirmed(selectedPaymentMethod, paidAmount, changeAmount)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Konfirmasi Pembayaran", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Checkout Pembayaran",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryRedText,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    "Total Tagihan Anda",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    currencyFormatter.format(totalPrice),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryRedText
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Pilih Metode Pembayaran:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PaymentMethodButton(text = "TUNAI", icon = Icons.Filled.Money, isSelected = selectedPaymentMethod == MetodePembayaran.CASH, onClick = { selectedPaymentMethod = MetodePembayaran.CASH }, modifier = Modifier.weight(1f))
                            PaymentMethodButton(text = "QRIS", icon = Icons.Filled.QrCodeScanner, isSelected = selectedPaymentMethod == MetodePembayaran.QRIS, onClick = { selectedPaymentMethod = MetodePembayaran.QRIS }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = selectedPaymentMethod == MetodePembayaran.QRIS) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Scan QRIS Pembayaran", style = MaterialTheme.typography.titleMedium, color = PrimaryRedText, modifier = Modifier.padding(bottom=8.dp))
                            Text("Tunjukkan QR Code ini ke pelanggan atau scan dari e-wallet Anda.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom=16.dp))
                            Image(
                                painter = painterResource(id = R.drawable.qris_code),
                                contentDescription = "QR Code Pembayaran",
                                modifier = Modifier
                                    .size(280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, ButtonRed.copy(alpha=0.5f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = selectedPaymentMethod == MetodePembayaran.CASH) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Pembayaran Tunai", style = MaterialTheme.typography.titleLarge, color = PrimaryRedText)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = cashPaidAmountString,
                                onValueChange = { newValue -> cashPaidAmountString = newValue.filter { it.isDigit() } },
                                label = { Text("Jumlah Uang Diterima") },
                                prefix = { Text("Rp") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ButtonRed)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Kembalian: ${currencyFormatter.format(changeAmount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if ((cashPaidAmountString.toDoubleOrNull() ?: 0.0) >= totalPrice) PrimaryRedText else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) ButtonRed else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else PrimaryRedText
    val borderColor = if (isSelected) ButtonRed.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = if (!isSelected) BorderStroke(1.dp, borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CheckoutScreenCashSelectedPreview() {
    MaterialTheme {
        CheckoutScreen(
            totalPrice = 75000.0,
            checkoutItems = listOf(CartItem(Menu(1,"Dummy Menu",75000.0, deskripsi = " enak", imageUri = null),1)),
            onPaymentConfirmed = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CheckoutScreenQRISSelectedPreview() {
    MaterialTheme {
        CheckoutScreen(
            totalPrice = 50000.0,
            checkoutItems = listOf(CartItem(Menu(1,"Dummy Menu",50000.0, deskripsi = "mantap", imageUri = null),1)),
            onPaymentConfirmed = { _, _, _ -> }
        )
    }
}