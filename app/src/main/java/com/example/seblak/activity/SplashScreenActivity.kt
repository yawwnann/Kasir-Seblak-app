package com.example.seblak.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seblak.R
import com.example.seblak.ui.common.DecorativeRedBackground
import com.example.seblak.ui.theme.PrimaryRedText
import kotlinx.coroutines.delay

private const val ENTER_ANIMATION_DURATION = 1000
private const val EXIT_ANIMATION_DURATION = 700
private const val CONTENT_APPEAR_DELAY = 200L

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ComponentActivity() {

    private val SPLASH_TOTAL_DURATION: Long = 3500 // Total durasi termasuk animasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedSplashScreenLayout (
                    totalSplashTime = SPLASH_TOTAL_DURATION,
                    onTimeout = {
                        val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AnimatedSplashScreenLayout(totalSplashTime: Long, onTimeout: () -> Unit) {
    var elementsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        delay(CONTENT_APPEAR_DELAY)
        elementsVisible = true

        val mainContentDisplayTime = totalSplashTime - CONTENT_APPEAR_DELAY - ENTER_ANIMATION_DURATION - EXIT_ANIMATION_DURATION
        if (mainContentDisplayTime > 0) {
            delay(mainContentDisplayTime)
        } else {
            // Jika totalSplashTime terlalu pendek, pastikan setidaknya animasi masuk sempat berjalan penuh
            // sebelum mulai animasi keluar. Ini bisa terjadi jika totalSplashTime < CONTENT_APPEAR_DELAY + ENTER_ANIMATION_DURATION + EXIT_ANIMATION_DURATION
            val minimumDisplayAfterEnter = 500L // Waktu minimal konten terlihat setelah animasi masuk
            val adjustedDelay = ENTER_ANIMATION_DURATION + CONTENT_APPEAR_DELAY + minimumDisplayAfterEnter - (totalSplashTime - EXIT_ANIMATION_DURATION)
            if (adjustedDelay > 0 && (totalSplashTime - CONTENT_APPEAR_DELAY - EXIT_ANIMATION_DURATION) > ENTER_ANIMATION_DURATION){
                // Hanya delay jika masih ada waktu setelah animasi masuk dan sebelum animasi keluar
            } else if ((totalSplashTime - CONTENT_APPEAR_DELAY - EXIT_ANIMATION_DURATION) > 0) {
                delay(totalSplashTime - CONTENT_APPEAR_DELAY - EXIT_ANIMATION_DURATION)
            }
            // Untuk kasus totalSplashTime sangat pendek, mungkin perlu logika yang lebih kompleks
            // atau peringatan bahwa durasinya tidak cukup untuk semua animasi.
            // Untuk sekarang, kita biarkan agar setidaknya animasi keluar tetap berjalan.
        }


        elementsVisible = false
        delay(EXIT_ANIMATION_DURATION.toLong())
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DecorativeRedBackground()

        AnimatedVisibility(
            visible = elementsVisible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(durationMillis = ENTER_ANIMATION_DURATION)) +
                    scaleIn(
                        animationSpec = tween(durationMillis = ENTER_ANIMATION_DURATION, delayMillis = 100),
                        initialScale = 0.7f
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = EXIT_ANIMATION_DURATION)) +
                    scaleOut(
                        animationSpec = tween(durationMillis = EXIT_ANIMATION_DURATION),
                        targetScale = 0.7f
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logoseblak),
                    contentDescription = "Logo Aplikasi Seblak Titik Koma",
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Seblak Titik Koma",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRedText
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimatedSplashScreenPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedSplashScreenLayout(totalSplashTime = 3500L) {}
    }
}