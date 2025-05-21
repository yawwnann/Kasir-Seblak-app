package com.example.seblak.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.R
import com.example.seblak.db.UserDao
import com.example.seblak.ui.common.DecorativeRedBackground
// import com.example.seblak.ui.theme.SeblakTheme // Sesuaikan dengan tema Anda
// import com.example.seblak.ui.theme.YourAppRed // Jika Anda mendefinisikan warna merah di tema

// Definisikan beberapa nuansa merah (Anda bisa menyesuaikannya atau mengambil dari tema)
val LightRedBackground = Color(0xFFF8BBD0).copy(alpha = 0.25f) // Merah muda sangat terang
val MediumRedBackground = Color(0xFFE57373).copy(alpha = 0.20f) // Merah lembut
val AccentRedBackground = Color(0xFFEF9A9A).copy(alpha = 0.18f) // Aksen merah terang lainnya

val PrimaryRedText = Color(0xFFC62828) // Merah tua untuk teks judul
val ButtonRed = Color(0xFFD32F2F)      // Merah untuk tombol utama

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDao = UserDao(this)
        setContent {
            // SeblakTheme { // Gunakan tema aplikasi Anda
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background // Background utama bisa tetap netral
            ) {
                LoginScreenWithRedTheme( // Menggunakan fungsi Composable yang baru
                    userDao = userDao,
                    onLoginSuccess = {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                )
            }
            // }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenWithRedTheme(
    userDao: UserDao,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeRedBackground() // Latar belakang dengan nuansa merah

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logoseblak), // GANTI JIKA NAMA LOGO BEDA
                contentDescription = "Logo Seblak",
                modifier = Modifier
                    .size(140.dp) // Ukuran logo disesuaikan agar tidak terlalu besar
                    .padding(bottom = 16.dp)
            )

            Text(
                "Selamat Datang!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryRedText // Menggunakan warna merah tua untuk judul
            )
            Text(
                "Login untuk melanjutkan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Bisa tetap default atau disesuaikan
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors( // Sesuaikan warna outline field jika perlu
                    focusedBorderColor = ButtonRed, // Warna border saat fokus
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ButtonRed,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                    } else {
                        if (userDao.verifyLogin(username, password)) {
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Username atau password salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed, // Tombol login menggunakan warna merah
                    contentColor = Color.White // Teks tombol menjadi putih agar kontras
                )
            ) {
                Text("Login", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRedText.copy(alpha = 0.8f)) // Link register juga nuansa merah
            ) {
                Text("Belum punya akun? Register di sini")
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenWithRedThemePreview() { // Nama preview juga diubah
    // SeblakTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LoginScreenWithRedTheme(
            userDao = UserDao(LocalContext.current),
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
    // }
}