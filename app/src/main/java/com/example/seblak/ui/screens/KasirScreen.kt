package com.example.seblak.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.seblak.R
import com.example.seblak.activity.CheckoutActivity
import com.example.seblak.model.CartItem
import com.example.seblak.model.Menu
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
import com.example.seblak.viewmodel.CartViewModel
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.ArrayList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirContentScreen(cartViewModel: CartViewModel = viewModel()) { // <--- PASTIKAN PARAMETER INI ADA (atau viewModel() jika dipanggil tanpa parameter)
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalCartPrice.collectAsState()
    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    Scaffold(
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)).padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PrimaryRedText)
                        Text(currencyFormatter.format(totalPrice), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PrimaryRedText)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty() && totalPrice > 0) {
                                val intent = Intent(context, CheckoutActivity::class.java).apply {
                                    putExtra(CheckoutActivity.EXTRA_TOTAL_PRICE, totalPrice)
                                    putParcelableArrayListExtra(CheckoutActivity.EXTRA_CART_ITEMS_LIST, ArrayList(cartItems))
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        shape = MaterialTheme.shapes.medium,
                        enabled = cartItems.isNotEmpty() && totalPrice > 0
                    ) { Text("Checkout (Simpan Transaksi)", fontSize = 16.sp, color = Color.White) }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { cartViewModel.clearCart() },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, ButtonRed),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ButtonRed)
                    ) { Text("Kosongkan Keranjang") }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Keranjang Anda masih kosong.", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = cartItems, key = { cartItem -> cartItem.menu.id }) { item ->
                    CartListItem(
                        cartItem = item,
                        currencyFormatter = currencyFormatter,
                        onIncrement = { cartViewModel.incrementItemQuantity(item.menu.id) },
                        onDecrement = { cartViewModel.decrementItemQuantity(item.menu.id) },
                        onRemove = { cartViewModel.removeItemFromCart(item.menu.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartListItem(
    cartItem: CartItem,
    currencyFormatter: NumberFormat,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val imagePath = cartItem.menu.imageUri
            if (imagePath != null && imagePath.isNotBlank()) {
                val imageFile = remember(imagePath) { File(imagePath) }
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageFile)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.ic_error_image).build()
                    ),
                    contentDescription = cartItem.menu.nama,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.RestaurantMenu, "Placeholder", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.menu.nama, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2, overflow = TextOverflow.Ellipsis, color = PrimaryRedText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(currencyFormatter.format(cartItem.menu.harga), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Subtotal: ${currencyFormatter.format(cartItem.subtotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.RemoveCircleOutline, "Kurangi", tint = ButtonRed) }
                    Text("${cartItem.quantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 6.dp))
                    IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.AddCircleOutline, "Tambah", tint = ButtonRed) }
                }
                TextButton(onClick = onRemove, modifier = Modifier.height(32.dp).offset(y = (-4).dp) ) {
                    Text("Hapus", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KasirContentScreenPreview() {
    // SeblakTheme {
    KasirContentScreen(cartViewModel = viewModel())
    // }
}