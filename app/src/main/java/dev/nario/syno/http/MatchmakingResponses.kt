package dev.nario.syno.http

data class MatchedPlayer(
    val id: Int,
    val user_firebase_id: String,
    val contact: String,
    val latitude: Double,
    val longitude: Double,
    val distancia_km: Double
)

data class FindNearbyPlayerResponse(
    val findedUser: MatchedPlayer,
    val matchId: Int
)

data class RegisterUserResponse(
    val createdUserId: Int
)

data class ApiErrorResponse(
    val message: String
)
