package com.example.seblak.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.seblak.R
import com.example.seblak.db.MenuDao
import com.example.seblak.model.Menu
import com.example.seblak.ui.common.DecorativeRedBackground // Asumsi ini sudah ada dan diimport
import com.example.seblak.ui.theme.ButtonRed
import com.example.seblak.ui.theme.PrimaryRedText
// import com.example.seblak.ui.theme.SeblakTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import saveImageToInternalStorage


// Tambahkan dependensi accompanist-permissions jika belum
// implementation "com.google.accompanist:accompanist-permissions:0.34.0" // Cek versi terbaru

@OptIn(ExperimentalPermissionsApi::class)
class TambahMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val menuDao = MenuDao(this)
        setContent {
            // SeblakTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                TambahMenuScreen(
                    menuDao = menuDao,
                    onBackClicked = { finish() },
                    onMenuAdded = {
                        Toast.makeText(this, "Menu berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        finish() // Kembali ke halaman sebelumnya setelah menu ditambahkan
                    }
                )
            }
            // }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TambahMenuScreen(
    menuDao: MenuDao,
    onBackClicked: () -> Unit,
    onMenuAdded: () -> Unit
) {
    var namaMenu by remember { mutableStateOf("") }
    var hargaMenu by remember { mutableStateOf("") }
    var deskripsiMenu by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Permission for reading storage (needed for GetContent on older APIs, good practice)
    val readStoragePermission = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Menu Baru", color = PrimaryRedText) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = PrimaryRedText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            DecorativeRedBackground() // Menggunakan background yang sama

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()), // Agar bisa di-scroll jika form panjang
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Detail Item Menu",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryRedText,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (readStoragePermission.status.isGranted) {
                                imagePickerLauncher.launch("image/*")
                            } else if (readStoragePermission.status.shouldShowRationale) {
                                // Tampilkan dialog penjelasan mengapa izin dibutuhkan
                                // Untuk simplicity, kita langsung request lagi
                                readStoragePermission.launchPermissionRequest()
                            }
                            else {
                                readStoragePermission.launchPermissionRequest()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Gambar Menu Terpilih",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = "Pilih Gambar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Ketuk untuk memilih gambar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = namaMenu,
                    onValueChange = { namaMenu = it },
                    label = { Text("Nama Menu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ButtonRed)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = hargaMenu,
                    onValueChange = { hargaMenu = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ButtonRed)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = deskripsiMenu,
                    onValueChange = { deskripsiMenu = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Untuk deskripsi multiline
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ButtonRed)
                )
                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (namaMenu.isBlank() || hargaMenu.isBlank()) {
                            Toast.makeText(context, "Nama menu dan harga harus diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val hargaDouble = hargaMenu.toDoubleOrNull()
                        if (hargaDouble == null || hargaDouble <= 0) {
                            Toast.makeText(context, "Harga tidak valid", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        var finalImageToStore: String? = null
                        imageUri?.let { uriFromPicker -> // imageUri adalah state Uri dari pemilih gambar
                            finalImageToStore = saveImageToInternalStorage(context, uriFromPicker, namaMenu)
                            if (finalImageToStore == null) {
                                Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                                // Anda bisa memutuskan apakah akan melanjutkan tanpa gambar atau tidak
                            }
                        }


                        val menuBaru = Menu(
                            nama = namaMenu,
                            harga = hargaDouble,
                            deskripsi = deskripsiMenu.ifBlank { null },
                            imageUri = finalImageToStore// Simpan URI sebagai String
                        )
                        val result = menuDao.tambahMenu(menuBaru)
                        if (result != -1L) {
                            onMenuAdded()
                        } else {
                            Toast.makeText(context, "Gagal menambahkan menu", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Simpan Menu", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp)) // Jarak di bawah tombol
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TambahMenuScreenPreview() {
    // SeblakTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
        TambahMenuScreen(
            menuDao = MenuDao(LocalContext.current), // Perlu context untuk Dao
            onBackClicked = {},
            onMenuAdded = {}
        )
    }
    // }
}