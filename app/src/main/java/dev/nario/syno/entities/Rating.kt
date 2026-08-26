package dev.nario.syno.entities

data class Rating (
    val targetUserId: String = "",
    val voterId: String = "",
    val value: Int = 0
)