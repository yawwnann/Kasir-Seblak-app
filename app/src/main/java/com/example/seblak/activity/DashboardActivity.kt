package com.example.seblak.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.seblak.db.MenuDao
import com.example.seblak.db.UserDao
import com.example.seblak.model.Menu
import com.example.seblak.ui.theme.PrimaryRedText
import com.example.seblak.ui.theme.ButtonRed
import java.text.NumberFormat
import java.util.Locale

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dasbor : BottomNavItem("dasbor", "Dasbor", Icons.Filled.Home)
    object Menu : BottomNavItem("menu", "Menu", Icons.Filled.RestaurantMenu)
    object Kasir : BottomNavItem("kasir", "Kasir", Icons.Filled.PointOfSale)
    object Laporan : BottomNavItem("laporan", "Laporan", Icons.Filled.Assessment)
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDao = UserDao(this)
        val menuDao = MenuDao(this)
        setContent {
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
        topBar = {
            TopAppBar(
                title = { Text("Kasir Seblak - ${currentScreen.title}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogoutClicked) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = PrimaryRedText)
                    }
                }
            )
        },
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
        Button(onClick = { /*TODO*/ }) { Text("Fitur Dasbor 1") }
        Button(onClick = { /*TODO*/ }) { Text("Fitur Dasbor 2") }
    }
}

@Composable
fun MenuContentScreen(menuDao: MenuDao, onNavigateToTambahMenu: () -> Unit) {
    var menuList by remember { mutableStateOf(emptyList<Menu>()) }

    LaunchedEffect(Unit) {
        menuList = menuDao.getAllMenu()
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
        if (menuList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValuesFromScaffold)
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValuesFromScaffold),
                contentPadding = PaddingValues(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuList) { menu ->
                    MenuItemCard(menu = menu)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                        model = Uri.parse(menu.imageUri)
                    ),
                    contentDescription = menu.nama,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp)),
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
    MainDashboardScreen(
        menuDao = MenuDao(LocalContext.current),
        onLogoutClicked = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    val navItems = listOf(BottomNavItem.Dasbor, BottomNavItem.Menu, BottomNavItem.Kasir, BottomNavItem.Laporan)
    AppBottomNavigationBar(currentScreen = BottomNavItem.Dasbor, onItemSelected = {}, navItems = navItems)
}

@Preview(showBackground = true, name = "Menu Item Card Preview")
@Composable
fun MenuItemCardPreview() {
    MenuItemCard(menu = Menu(1, "Seblak Komplit Istimewa", 25000.0, "Seblak super komplit dengan topping melimpah ruah, rasa pedas nampol.", imageUri = null))
}

@Preview(showBackground = true, name = "Menu Content Screen Preview")
@Composable
fun MenuContentScreenPreview() {
    val context = LocalContext.current
    MenuContentScreen(menuDao = MenuDao(context), onNavigateToTambahMenu = {})
}