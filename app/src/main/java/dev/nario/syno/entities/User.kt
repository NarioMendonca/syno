package dev.nario.syno.entities

import java.math.BigDecimal

data class User (
    val id: String,
    val name: String,
    val email: String,
    val isEmailVerified: Boolean,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val isOnline: Boolean
)