package com.example.seblak.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import android.widget.Toast
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.seblak.R
import com.example.seblak.db.MenuDao
import com.example.seblak.model.Menu
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

const val TAG_MENU_SCREEN = "MenuScreen"
const val TAG_MENU_ITEM_CARD = "MenuItemCard"

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
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                menuToDelete = null
            },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Anda yakin ingin menghapus menu '${menuToDelete?.nama}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            menuToDelete?.let { item ->
                                Log.d(TAG_MENU_SCREEN, "Deleting menu item: ${item.nama}, ID: ${item.id}")
                                withContext(Dispatchers.IO) {
                                    menuDao.deleteMenu(item.id)
                                    item.imageUri?.let { path -> File(path).delete() }
                                }
                                Log.d(TAG_MENU_SCREEN, "Menu item deleted. Triggering refresh.")
                                Toast.makeText(context, "'${item.nama}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                                refreshTrigger++
                            }
                        }
                        showDeleteDialog = false
                        menuToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        menuToDelete = null
                    }
                ) {
                    Text("Batal")
                }
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
                    Log.d(TAG_MENU_SCREEN, "Showing loading indicator.")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorLoading -> {
                    Log.d(TAG_MENU_SCREEN, "Showing error loading message.")
                    Text(
                        "Gagal memuat menu. Coba lagi nanti.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                menuList.isEmpty() -> {
                    Log.d(TAG_MENU_SCREEN, "Menu list is empty. Showing empty state message.")
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
                    Log.d(TAG_MENU_SCREEN, "Displaying menu list with ${menuList.size} items.")
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = menuList, key = { menu -> menu.id }) { menu ->
                            MenuItemCard(
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
fun MenuItemCard(
    menu: Menu,
    onAddToCartClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }
    Log.d(TAG_MENU_ITEM_CARD, "Rendering MenuItemCard for: ${menu.nama}, Image Path: ${menu.imageUri}")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!menu.imageUri.isNullOrBlank()) {
                Log.d(TAG_MENU_ITEM_CARD, "Attempting to load image for ${menu.nama} from Path: ${menu.imageUri}")
                val context = LocalContext.current
                val imageFile = remember(menu.imageUri) { File(menu.imageUri!!) }

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
                            is AsyncImagePainter.State.Error -> Log.e(TAG_MENU_ITEM_CARD, "Painter State: Error for ${menu.nama}. Path: ${menu.imageUri}", state.result.throwable)
                            else -> Log.d(TAG_MENU_ITEM_CARD, "Painter State: ${state::class.java.simpleName} for ${menu.nama}")
                        }
                    }
                )

                Image(
                    painter = painter,
                    contentDescription = menu.nama,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Log.d(TAG_MENU_ITEM_CARD, "Image URI is null or blank for ${menu.nama}. Showing placeholder icon.")
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp))
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menu.nama,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryRedText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormatter.format(menu.harga),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
                if (!menu.deskripsi.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = menu.deskripsi ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onAddToCartClicked) {
                        Icon(
                            Icons.Filled.AddShoppingCart,
                            contentDescription = "Tambah ke Keranjang",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClicked) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus Menu",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Menu Content Screen - Empty")
@Composable
fun MenuContentScreenEmptyPreview() {
    MaterialTheme {
        Surface {
            MenuContentScreen(menuDao = MenuDao(LocalContext.current), onNavigateToTambahMenu = {})
        }
    }
}

@Preview(showBackground = true, name = "Menu Content Screen - With Data")
@Composable
fun MenuContentScreenWithDataPreview() {
    val context = LocalContext.current
    val dummyMenuDao = object : MenuDao(context) {
        override fun getAllMenu(): List<Menu> {
            return listOf(
                Menu(1, "Seblak Original Pedas", 15000.0, "Seblak original dengan kerupuk kenyal, sayuran segar, dan level pedas pilihanmu.", null),
                Menu(2, "Seblak Ceker Mercon", 18000.0, "Seblak nikmat dengan tambahan ceker ayam empuk yang melimpah dan bumbu mercon.", "/data/user/0/com.example.seblak/files/menu_img_Seblak_Ceker_Mercon_1621600000000.jpg"),
                Menu(3, "Seblak Seafood Komplit", 25000.0, "Perpaduan aneka seafood segar dalam kuah seblak yang kaya rasa.", null)
            )
        }
    }
    MaterialTheme {
        Surface {
            MenuContentScreen(menuDao = dummyMenuDao, onNavigateToTambahMenu = {})
        }
    }
}

@Preview(showBackground = true, name = "Menu Item Card Preview - With Image Path")
@Composable
fun MenuItemCardPreviewWithImagePath() {
    MaterialTheme {
        MenuItemCard(
            menu = Menu(1, "Seblak Komplit Istimewa", 25000.0, "Seblak super komplit dengan topping melimpah ruah, rasa pedas nampol.", imageUri = "/data/user/0/com.example.seblak/files/menu_img_preview.jpg"),
            onAddToCartClicked = {},
            onDeleteClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Menu Item Card Preview - No Image")
@Composable
fun MenuItemCardPreviewNoImage() {
    MaterialTheme {
        MenuItemCard(
            menu = Menu(1, "Seblak Tanpa Gambar", 12000.0, "Seblak polosan tanpa gambar.", imageUri = null),
            onAddToCartClicked = {},
            onDeleteClicked = {}
        )
    }
}