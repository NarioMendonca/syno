package dev.nario.syno.entities

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Match (
    val id: String,
    val creatorId: String,
    val name: String,
    val game: String,
    val date: Date,
    val time: Date,
    val maxPlayers: Int,
    val meetingLocation: String,
    val averageAge: Int?,
    val gameType: String?,
    val participants: List<String>,
    @ServerTimestamp
    val createdAt: Date? = null
)