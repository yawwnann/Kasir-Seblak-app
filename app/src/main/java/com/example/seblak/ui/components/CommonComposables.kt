package com.example.seblak.ui.common // Sesuaikan dengan nama package yang Anda buat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

// Import konstanta warna terpusat Anda
// PASTIKAN PATH PACKAGE INI BENAR
import com.example.seblak.ui.theme.LightRedBackground
import com.example.seblak.ui.theme.MediumRedBackground
import com.example.seblak.ui.theme.AccentRedBackground

@Composable
fun DecorativeRedBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-90).dp, y = (-120).dp)
                .size(280.dp)
                .background(
                    LightRedBackground,
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (80).dp, y = (100).dp)
                .size(320.dp)
                .background(
                    MediumRedBackground,
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (60).dp, y = (180).dp)
                .size(130.dp)
                .rotate(25f)
                .background(
                    AccentRedBackground,
                    RoundedCornerShape(35.dp)
                )
        )
    }
}

// Anda bisa menambahkan Composable bersama lainnya di file ini nanti