package com.example.snapfine

import com.google.firebase.firestore.PropertyName

data class Violation(
    val vehicleNumber: String = "",
    val reportedBy: String = "",
    val imageUrl: String = "",
    val type: String = "",
    val location: String = "",
    val description: String = "",
    val fineAmount: String = "",
    val status: String = "pending",
    val time: String = "",
    val date: String = "",
    val timestamp: Long = 0,
    val approvedBy: String = "",
    val rejectionReason: String = "",
    val violatorUid: String = ""
) {
    // Legacy support properties (automatically populated by Firestore if found)
    
    @set:PropertyName("violationType")
    @get:PropertyName("violationType")
    var violationType: String? = null

    @set:PropertyName("evidenceUrl")
    @get:PropertyName("evidenceUrl")
    var evidenceUrl: String? = null

    @set:PropertyName("reportedByUID")
    @get:PropertyName("reportedByUID")
    var reportedByUID: String? = null

    /**
     * Helper to get the canonical type (prefers new 'type' field, fallbacks to legacy)
     */
    fun getSafeType(): String {
        return if (type.isNotEmpty()) type else violationType ?: "Traffic Violation"
    }

    /**
     * Helper to get the canonical Image URL (prefers new 'imageUrl' field, fallbacks to legacy)
     */
    fun getSafeImageUrl(): String {
        return if (imageUrl.isNotEmpty()) imageUrl else evidenceUrl ?: ""
    }
}
