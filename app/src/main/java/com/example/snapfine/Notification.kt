package com.example.snapfine

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "violation",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
