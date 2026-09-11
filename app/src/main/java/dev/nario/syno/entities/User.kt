package dev.nario.syno.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    @DocumentId
    val id: String = "",
    val name: String = "",
    val profileDescription: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false,
    val photoUrl: String = "",
    val rating: Double = 0.0,
    val votesCount: Int = 0,
    val votes: List<Rating> = emptyList(),
    val favoriteGame: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isOnline: Boolean = false
)