// Pastikan import ini ada di bagian atas file Anda (misal DashboardActivity.kt atau MenuScreen.kt)
// Tambahkan jika belum ada:
import android.content.Intent // Sudah ada jika di DashboardActivity.kt
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu // Ganti dari Icons.Filled.Restaurant jika ini yang dimaksud
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Untuk loading state
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.rememberAsyncImagePainter
import com.example.seblak.R
import com.example.seblak.db.MenuDao // Pastikan path ini benar
import com.example.seblak.model.Menu // Pastikan path ini benar
import com.example.seblak.ui.theme.ButtonRed // Pastikan path ini benar
import com.example.seblak.ui.theme.PrimaryRedText // Pastikan path ini benar
import kotlinx.coroutines.Dispatchers // Untuk coroutines
import kotlinx.coroutines.withContext // Untuk coroutines
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.DeleteOutline // Ikon untuk delete
import androidx.compose.material.icons.filled.AddShoppingCart

@Composable
fun MenuContentScreen(menuDao: MenuDao, onNavigateToTambahMenu: () -> Unit) {
    var menuList by remember { mutableStateOf(emptyList<Menu>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorLoading = false
        try {
            menuList = withContext(Dispatchers.IO) {
                menuDao.getAllMenu()
            }
        } catch (e: Exception) {
            // Tangani error, misalnya dengan menampilkan pesan
            errorLoading = true
            // Log error: Log.e("MenuContentScreen", "Error fetching menu", e)
        } finally {
            isLoading = false
        }
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = menuList, key = { menu -> menu.id }) { menu ->
                            MenuItemCard(menu = menu)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(menu: Menu) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }

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
                Image(
                    painter = rememberAsyncImagePainter(
                        model = try {
                            Uri.parse(menu.imageUri)
                        } catch (e: Exception) {
                            // Log.e("MenuItemCard", "Invalid URI format: ${menu.imageUri}", e)
                            null // Kembalikan null jika URI tidak valid
                        },
                        placeholder = painterResource(id = R.drawable.placeholder_image), // Ganti dengan drawable Anda
                        error = painterResource(id = R.drawable.ic_error_image) // Ganti dengan drawable Anda
                    ),
                    contentDescription = menu.nama,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.RestaurantMenu,
                        contentDescription = "Placeholder Menu",
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
                        text = menu.deskripsi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Menu Content Screen - Empty")
@Composable
fun MenuContentScreenEmptyPreview() {
    val context = LocalContext.current
    var menuList by remember { mutableStateOf(emptyList<Menu>()) }
    var isLoading by remember { mutableStateOf(false) } // Set isLoading ke false untuk preview empty state

    // SeblakTheme { // Aktifkan tema Anda
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = ButtonRed) {
                Icon(Icons.Filled.Add, "Tambah Menu Baru", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
        } else if (menuList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Belum ada menu.\nSilakan tambahkan menu baru dengan tombol (+).",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuList) { menu ->
                    MenuItemCard(menu = menu)
                }
            }
        }
    }
    // }
}

@Preview(showBackground = true, name = "Menu Content Screen - With Data")
@Composable
fun MenuContentScreenWithDataPreview() {
    val context = LocalContext.current
    val dummyMenuList = listOf(
        Menu(1, "Seblak Original Pedas", 15000.0, "Seblak original dengan kerupuk kenyal, sayuran segar, dan level pedas pilihanmu.", null),
        Menu(2, "Seblak Ceker Mercon", 18000.0, "Seblak nikmat dengan tambahan ceker ayam empuk yang melimpah dan bumbu mercon.", "content://com.example.seblak.fileprovider/cache/picked_image.jpg"), // Contoh URI
        Menu(3, "Seblak Seafood Komplit", 25000.0, "Perpaduan aneka seafood segar dalam kuah seblak yang kaya rasa.", null)
    )
    var menuList by remember { mutableStateOf(dummyMenuList) }
    var isLoading by remember { mutableStateOf(false) }

    // SeblakTheme { // Aktifkan tema Anda
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = ButtonRed) {
                Icon(Icons.Filled.Add, "Tambah Menu Baru", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuList) { menu ->
                MenuItemCard(menu = menu)
            }
        }
    }
    // }
}

@Preview(showBackground = true, name = "Menu Item Card Preview")
@Composable
fun MenuItemCardPreview() {
    // SeblakTheme {
    MenuItemCard(menu = Menu(1, "Seblak Komplit Istimewa", 25000.0, "Seblak super komplit dengan topping melimpah ruah, rasa pedas nampol.", imageUri = null))
    // }
}