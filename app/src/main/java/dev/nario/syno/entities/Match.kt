package dev.nario.syno.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.sql.Time
import java.time.LocalDateTime
import java.util.Date

@IgnoreExtraProperties
data class Match (
    @DocumentId
    var id: String? = null,
    val creatorId: String = "",
    val name: String = "",
    val game: String = "",
    val scheduledAt: Timestamp = Timestamp.now(),
    val maxPlayers: Int = 0,
    val meetingLocation: String = "",
    val averageAge: Int? = null,
    val gameType: String? = null,
    val participants: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
)