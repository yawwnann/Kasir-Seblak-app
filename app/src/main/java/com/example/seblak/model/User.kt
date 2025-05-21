package com.example.seblak.model

data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String // Pastikan nama parameter ini adalah 'passwordHash'
)