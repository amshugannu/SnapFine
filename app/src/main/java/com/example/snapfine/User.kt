package com.example.snapfine

data class User(
    val Name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = "citizen",
    val verificationStatus: String? = "approved",
    val adminLevel: Int? = null,
    val VehicleNumber: String? = null,
    val VehicleType: String? = null,
    val VehicleRegistrationId: String? = null,
    val licenseNumber: String? = null
)
