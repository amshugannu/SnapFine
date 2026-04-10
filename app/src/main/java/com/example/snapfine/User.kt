package com.example.snapfine

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("Name") @set:PropertyName("Name")
    var name: String? = null,
    
    var email: String? = null,
    var phone: String? = null,
    var role: String? = "citizen",
    var isVerified: Boolean = false,
    var verificationStatus: String? = "pending",
    var adminLevel: Int? = null,

    var vehicleNumber: String? = null,
    var vehicleType: String? = null,
    var vehicleRegistrationId: String? = null,
    var licenseNumber: String? = null,
    var fcmToken: String? = null
)
