package dev.nario.syno.entities

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false,
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isOnline: Boolean = false
)