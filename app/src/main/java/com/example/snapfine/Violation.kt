package com.example.snapfine

data class Violation(
    val vehicleNumber: String = "",
    val reportedByUID: String = "",
    val reportedToUID: String = "",
    val imageUrl: String = "",
    val violationType: String = "",
    val location: String = "",
    val description: String = "",
    val fineAmount: String = "",
    val status: String = "",
    val time: String = "",
    val date: String = "",
    val timestamp: Long = 0
)
