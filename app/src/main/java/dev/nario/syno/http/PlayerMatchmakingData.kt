package dev.nario.syno.http

data class PlayerMatchmakingData(
    val firebase_id: String,
    val contact: String,
    val latitude: Double,
    val longitude: Double,
    val distanceLimitToSearch: Int,
    val preferences: List<String>
)
