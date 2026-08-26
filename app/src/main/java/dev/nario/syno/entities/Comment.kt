package dev.nario.syno.entities

import java.util.Date

data class Comment(
    val id: String = "",
    val targetUserId: String = "",
    val authorId: String = "",
    val text: String = "",
    val rating: Int = 0,
    val createdAt: Date? = null
)