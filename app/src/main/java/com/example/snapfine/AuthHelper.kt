package com.example.snapfine

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

fun routeUserBasedOnRole(activity: Activity, userId: String) {
    val fStore = FirebaseFirestore.getInstance()
    fStore.collection("users").document(userId).get()
        .addOnSuccessListener { doc ->
            val role = doc.getString("role") ?: "citizen"
            val verificationStatus = doc.getString("verificationStatus") ?: "approved"
            UserSession.updateSession(role, verificationStatus)
            val intent = when (role) {
                "staff" -> Intent(activity, StaffHomeActivity::class.java)
                "admin" -> Intent(activity, AdminHomeActivity::class.java)
                else -> Intent(activity, HomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
        .addOnFailureListener {
            Toast.makeText(activity, "Failed to fetch user data. Defaulting to citizen.", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
}
