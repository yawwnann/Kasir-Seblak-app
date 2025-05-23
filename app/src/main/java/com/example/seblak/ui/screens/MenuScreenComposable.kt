package com.example.seblak.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.seblak.R
import com.example.seblak.db.MenuDao
import com.example.seblak.model.Menu
import com.example.seblak.ui.common.DecorativeRedBackground // Pastikan ini diimpor
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
import com.example.seblak.viewmodel.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

const val TAG_MENU_CONTENT_SCREEN = "MenuContentScreen" // Nama tag sudah diubah
const val TAG_MENU_ITEM_DISPLAY_CARD = "MenuItemDisplayCard"

@Composable
fun MenuContentScreen(
    menuDao: MenuDao,
    cartViewModel: CartViewModel,
    onNavigateToTambahMenu: () -> Unit
) {
    var menuList by remember { mutableStateOf(emptyList<Menu>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<Menu?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        errorLoading = false
        try {
            Log.d(TAG_MENU_CONTENT_SCREEN, "Fetching menu. Trigger: $refreshTrigger")
            menuList = withContext(Dispatchers.IO) {
                menuDao.getAllMenu()
            }
            Log.d(TAG_MENU_CONTENT_SCREEN, "Menu fetched. Count: ${menuList.size}")
        } catch (e: Exception) {
            Log.e(TAG_MENU_CONTENT_SCREEN, "Error fetching menu", e)
            errorLoading = true
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog && menuToDelete != null) {
        val itemToDeleteCurrently = menuToDelete
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                menuToDelete = null
            },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Anda yakin ingin menghapus menu '${itemToDeleteCurrently?.nama}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            itemToDeleteCurrently?.let { item ->
                                var deleteSuccessInDb = false
                                withContext(Dispatchers.IO) {
                                    val rowsAffected = menuDao.deleteMenu(item.id)
                                    if (rowsAffected > 0) {
                                        deleteSuccessInDb = true
                                        item.imageUri?.let { path ->
                                            try {
                                                val imageFile = File(path)
                                                if (imageFile.exists() && imageFile.delete()) {
                                                    Log.d(TAG_MENU_CONTENT_SCREEN, "Image file deleted: $path")
                                                } else {
                                                    Log.w(TAG_MENU_CONTENT_SCREEN, "Failed to delete or file not found: $path")
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG_MENU_CONTENT_SCREEN, "Error deleting image file: $path", e)
                                            }
                                        }
                                    } else {
                                        Log.e(TAG_MENU_CONTENT_SCREEN, "DB delete FAILED for ID: ${item.id}")
                                    }
                                }
                                if (deleteSuccessInDb) {
                                    Toast.makeText(context, "'${item.nama}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                                    refreshTrigger++
                                } else {
                                    Toast.makeText(context, "Gagal menghapus '${item.nama}'.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        showDeleteDialog = false
                        menuToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Ya, Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; menuToDelete = null }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTambahMenu,
                containerColor = ButtonRed,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Filled.Add, "Tambah Menu Baru")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValuesFromScaffold ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValuesFromScaffold)
        ) {
            DecorativeRedBackground() // Menambahkan background dekoratif

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRedText)
                    }
                }
                errorLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Gagal memuat menu.\nCoba lagi nanti.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                menuList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.RestaurantMenu,
                                contentDescription = "Menu Kosong",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum ada menu.\nSilakan tambahkan menu baru dengan tombol (+).",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 16.dp), // Padding keseluruhan grid
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = menuList, key = { menu -> menu.id }) { menu ->
                            MenuItemDisplayCard(
                                menu = menu,
                                onAddToCartClicked = {
                                    cartViewModel.addMenuToCart(menu)
                                    Toast.makeText(context, "${menu.nama} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteClicked = {
                                    menuToDelete = menu
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemDisplayCard(
    menu: Menu,
    onAddToCartClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(), // Akan mengisi lebar sel grid
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, hoveredElevation = 8.dp), // Tambah hoveredElevation
        shape = RoundedCornerShape(16.dp), // Sudut lebih rounded
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Warna dasar kartu
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Bagian Gambar dengan Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f) // Rasio aspek 4:3 untuk gambar
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                val imagePath = menu.imageUri
                if (imagePath != null && imagePath.isNotBlank()) {
                    val context = LocalContext.current
                    val imageFile = remember(imagePath) { File(imagePath) }
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(imageFile)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.ic_error_image)
                            .fallback(R.drawable.ic_error_image)
                            .crossfade(true)
                            .build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = menu.nama,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RestaurantMenu,
                            contentDescription = "Placeholder Menu Icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                // Gradient overlay di atas gambar (opsional, untuk estetika)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.4f)),
                                startY = 200f // Mulai gradient sedikit di atas bagian bawah
                            )
                        )
                )
            }

            // Kolom untuk Teks dan Tombol Aksi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = menu.nama,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryRedText, // Warna utama untuk nama menu
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormatter.format(menu.harga),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (!menu.deskripsi.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = menu.deskripsi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // Beri jarak setelah deskripsi
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, // Tombol di ujung-ujung
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Delete (Outlined agar tidak terlalu dominan)
                    OutlinedButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.height(38.dp), // Ukuran tombol
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus Menu",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        // Spacer(modifier = Modifier.width(4.dp))
                        // Text("Hapus", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }

                    // Tombol Add to Cart (Filled)
                    Button(
                        onClick = onAddToCartClicked,
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            Icons.Filled.AddShoppingCart,
                            contentDescription = "Tambah ke Keranjang",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                    }
                }
            }
        }
    }
}

// --- Preview Functions --- (Sesuaikan jika perlu)
@Preview(showBackground = true, name = "Menu Content Screen - Grid With Data")
@Composable
fun MenuContentScreenGridWithDataPreview() {
    val context = LocalContext.current
    val dummyMenuDao = object : MenuDao(context) {
        override fun getAllMenu(): List<Menu> {
            return listOf(
                Menu(1, "Seblak Ori Pedas Nampol", 15000.0, "Original pedasnya mantap.", null),
                Menu(2, "Seblak Ceker Gledek", 18000.0, "Ceker ayam empuk bumbu gledek.", "/data/user/0/com.example.seblak/files/menu_img_Seblak_Ceker_Mercon_1621600000000.jpg"),
                Menu(3, "Seblak Seafood Surga Dunia", 25000.0, "Aneka seafood segar dalam kuah kaya rasa.", null),
                Menu(4, "Seblak Sosis Bakso Ceria", 17000.0, "Sosis dan bakso pilihan untuk keceriaan.", null)
            )
        }
    }
    MaterialTheme { Surface { MenuContentScreen(menuDao = dummyMenuDao, cartViewModel = viewModel(), onNavigateToTambahMenu = {}) } }
}

@Preview(showBackground = true, widthDp = 180, name = "Menu Item Card (New Grid Look)")
@Composable
fun MenuItemDisplayCardNewGridPreview() {
    MaterialTheme {
        MenuItemDisplayCard(
            menu = Menu(1, "Seblak Istimewa Panjang Sekali Namanya", 25000.0, "Deskripsi super panjang sekali untuk tes.", imageUri = null),
            onAddToCartClicked = {},
            onDeleteClicked = {}
        )
    }
}