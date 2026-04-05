package com.example.snapfine

object UserSession {
    var role: String = "citizen"
    var verificationStatus: String = "approved"

    fun updateSession(roleStr: String?, verificationStr: String?) {
        role = roleStr ?: "citizen"
        verificationStatus = verificationStr ?: "approved"
    }

    fun isApproved(): Boolean {
        return verificationStatus.equals("approved", ignoreCase = true)
    }

    fun isPending(): Boolean {
        return verificationStatus.equals("pending", ignoreCase = true)
    }

    fun isRejected(): Boolean {
        return verificationStatus.equals("rejected", ignoreCase = true)
    }
}
