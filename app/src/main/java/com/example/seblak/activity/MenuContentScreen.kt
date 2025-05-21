package com.example.seblak.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells // Import untuk Grid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Import untuk Grid
import androidx.compose.foundation.lazy.grid.items // Import items untuk Grid
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.seblak.R
import com.example.seblak.db.MenuDao
import com.example.seblak.model.Menu
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

const val TAG_MENU_SCREEN = "MenuScreenComposables"
const val TAG_MENU_ITEM_CARD = "MenuItemCardComposables"

@Composable
fun MenuContentScreen(menuDao: MenuDao, onNavigateToTambahMenu: () -> Unit) {
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
            Log.d(TAG_MENU_SCREEN, "Attempting to fetch menu from DAO. Trigger: $refreshTrigger")
            menuList = withContext(Dispatchers.IO) {
                menuDao.getAllMenu()
            }
            Log.d(TAG_MENU_SCREEN, "Menu fetched successfully. Count: ${menuList.size}")
        } catch (e: Exception) {
            Log.e(TAG_MENU_SCREEN, "Error fetching menu", e)
            errorLoading = true
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog && menuToDelete != null) {
        val itemToDeleteCurrently = menuToDelete
        AlertDialog(
            onDismissRequest = {
                Log.d(TAG_MENU_SCREEN, "DIALOG: Dismissed")
                showDeleteDialog = false
                menuToDelete = null
            },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Anda yakin ingin menghapus menu '${itemToDeleteCurrently?.nama}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d(TAG_MENU_SCREEN, "DIALOG: Confirm button clicked for ${itemToDeleteCurrently?.nama}")
                        scope.launch {
                            itemToDeleteCurrently?.let { item ->
                                Log.d(TAG_MENU_SCREEN, "CONFIRM DELETE: Attempting to delete: ${item.nama}, ID: ${item.id}")
                                var deleteSuccessInDb = false
                                withContext(Dispatchers.IO) {
                                    val rowsAffected = menuDao.deleteMenu(item.id)
                                    if (rowsAffected > 0) {
                                        deleteSuccessInDb = true
                                        Log.d(TAG_MENU_SCREEN, "CONFIRM DELETE: DB delete successful for ID: ${item.id}, rows: $rowsAffected")
                                        item.imageUri?.let { path ->
                                            try {
                                                val imageFile = File(path)
                                                if (imageFile.exists()) {
                                                    if (imageFile.delete()) {
                                                        Log.d(TAG_MENU_SCREEN, "CONFIRM DELETE: Image file deleted: $path")
                                                    } else {
                                                        Log.w(TAG_MENU_SCREEN, "CONFIRM DELETE: Failed to delete image file: $path")
                                                    }
                                                } else {
                                                    Log.w(TAG_MENU_SCREEN, "CONFIRM DELETE: Image file not found for deletion: $path")
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG_MENU_SCREEN, "CONFIRM DELETE: Error deleting image file: $path", e)
                                            }
                                        }
                                    } else {
                                        Log.e(TAG_MENU_SCREEN, "CONFIRM DELETE: DB delete FAILED for ID: ${item.id}")
                                    }
                                }
                                if (deleteSuccessInDb) {
                                    Toast.makeText(context, "'${item.nama}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                                    refreshTrigger++
                                    Log.d(TAG_MENU_SCREEN, "CONFIRM DELETE: Refresh triggered. New trigger value: $refreshTrigger")
                                } else {
                                    Toast.makeText(context, "Gagal menghapus '${item.nama}' dari database.", Toast.LENGTH_LONG).show()
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
                TextButton(
                    onClick = {
                        Log.d(TAG_MENU_SCREEN, "DIALOG: Cancel button clicked")
                        showDeleteDialog = false
                        menuToDelete = null
                    }
                ) { Text("Batal") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTambahMenu,
                containerColor = ButtonRed,
                contentColor = Color.White
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
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorLoading -> {
                    Text(
                        "Gagal memuat menu. Coba lagi nanti.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                menuList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada menu.\nSilakan tambahkan menu baru dengan tombol (+).",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // <--- PENGATURAN DUA KOLOM
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 12.dp), // Padding keseluruhan grid
                        verticalArrangement = Arrangement.spacedBy(12.dp), // Jarak antar baris
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Jarak antar kolom
                    ) {
                        items(items = menuList, key = { menu -> menu.id }) { menu ->
                            MenuItemCard( // MenuItemCard tetap sama
                                menu = menu,
                                onAddToCartClicked = {
                                    Log.d(TAG_MENU_SCREEN, "Add to cart clicked for: ${menu.nama}")
                                    Toast.makeText(context, "${menu.nama} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteClicked = {
                                    Log.d(TAG_MENU_SCREEN, "Delete clicked for: ${menu.nama}")
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
fun MenuItemCard( // Definisi MenuItemCard tetap sama seperti sebelumnya (dengan tombol aksi di bawah)
    menu: Menu,
    onAddToCartClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(), // Ini akan membuat kartu mengisi lebar sel grid
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Top
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
                            .build(),
                        onState = { state ->
                            when (state) {
                                is AsyncImagePainter.State.Loading -> Log.d(TAG_MENU_ITEM_CARD, "Painter State: Loading for ${menu.nama}...")
                                is AsyncImagePainter.State.Success -> Log.d(TAG_MENU_ITEM_CARD, "Painter State: Success for ${menu.nama}. From: ${state.result.dataSource}")
                                is AsyncImagePainter.State.Error -> Log.e(TAG_MENU_ITEM_CARD, "Painter State: Error for ${menu.nama}. Path: $imagePath", state.result.throwable)
                                else -> Log.d(TAG_MENU_ITEM_CARD, "Painter State: ${state::class.java.simpleName} for ${menu.nama}")
                            }
                        }
                    )
                    Image(
                        painter = painter,
                        contentDescription = menu.nama,
                        modifier = Modifier
                            .fillMaxWidth() // Buat gambar mengisi lebar kartu di grid
                            .height(100.dp) // Atur tinggi gambar yang konsisten untuk grid
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)), // Hanya sudut atas jika gambar di paling atas
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RestaurantMenu,
                            contentDescription = "Placeholder Menu Icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            // Kolom untuk teks dan tombol sekarang di bawah gambar
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Spacer(modifier = Modifier.width(0.dp)) // Hapus spacer horizontal, karena gambar sudah di atas

                Column(modifier = Modifier.fillMaxWidth()) { // Kolom untuk teks detail (Nama, Harga, Deskripsi)
                    Text(
                        text = menu.nama,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), // Mungkin titleMedium lebih pas
                        color = PrimaryRedText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormatter.format(menu.harga),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), // Mungkin bodyLarge
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!menu.deskripsi.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = menu.deskripsi ?: "",
                            style = MaterialTheme.typography.bodySmall, // Deskripsi lebih kecil
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2, // Sesuaikan maxLines untuk deskripsi di grid
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Baris untuk tombol aksi, berada di bawah semua detail teks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // Padding atas sebelum tombol
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAddToCartClicked,
                        modifier = Modifier.size(36.dp) // Perkecil IconButton
                    ) {
                        Icon(
                            Icons.Filled.AddShoppingCart,
                            contentDescription = "Tambah ke Keranjang",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp) // Perkecil Icon
                        )
                    }
                    IconButton(
                        onClick = onDeleteClicked,
                        modifier = Modifier.size(36.dp) // Perkecil IconButton
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus Menu",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp) // Perkecil Icon
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Menu Content Screen Grid - With Data")
@Composable
fun MenuContentScreenGridPreview() {
    val context = LocalContext.current
    val dummyMenuDao = object : MenuDao(context) {
        override fun getAllMenu(): List<Menu> {
            return listOf(
                Menu(1, "Seblak Ori", 15000.0, "Seblak original pedas.", null),
                Menu(2, "Seblak Ceker", 18000.0, "Seblak ceker ayam.", "/data/user/0/com.example.seblak/files/menu_img_Seblak_Ceker_Mercon_1621600000000.jpg"),
                Menu(3, "Seblak Seafood", 25000.0, "Seblak seafood komplit.", null),
                Menu(4, "Seblak Sosis Bakso", 17000.0, "Seblak dengan sosis dan bakso.", null)
            )
        }
    }
    MaterialTheme {
        Surface {
            MenuContentScreen(menuDao = dummyMenuDao, onNavigateToTambahMenu = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 200) // Preview dengan lebar lebih kecil untuk satu kartu
@Composable
fun MenuItemCardGridPreview() {
    MaterialTheme {
        MenuItemCard(
            menu = Menu(1, "Seblak Komplit Istimewa Super Panjang Namanya", 25000.0, "Deskripsi yang cukup panjang untuk melihat bagaimana teks akan terpotong atau wrap di dalam kartu yang lebih sempit.", imageUri = null),
            onAddToCartClicked = {},
            onDeleteClicked = {}
        )
    }
}
