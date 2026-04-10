package com.example.snapfine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserVerificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: UserVerificationAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_verification)

        // Safety check
        if (UserSession.role != "staff" && UserSession.role != "admin") {
            Toast.makeText(this, "Access denied. Staff or Admin only.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewUsers)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        firestore = FirebaseFirestore.getInstance()

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchPendingUsers()
    }

    private fun fetchPendingUsers() {
        firestore.collection("users")
            .whereEqualTo("verificationStatus", "pending")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    val usersList = querySnapshot.documents.filter { doc ->
                        val roleStr = doc.getString("role")
                        if (roleStr == null || roleStr.equals("citizen", ignoreCase = true)) {
                            true
                        } else {
                            false
                        }
                    }.toMutableList()

                    if (usersList.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter = UserVerificationAdapter(usersList) { doc, isApproved ->
                            updateUserStatus(doc, isApproved)
                        }
                        recyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch users.", Toast.LENGTH_SHORT).show()
                tvEmptyState.visibility = View.VISIBLE
                tvEmptyState.text = "Error loading users"
                recyclerView.visibility = View.GONE
            }
    }

    private fun updateUserStatus(doc: DocumentSnapshot, isApproved: Boolean) {
        val newStatus = if (isApproved) "approved" else "rejected"
        firestore.collection("users").document(doc.id)
            .update("verificationStatus", newStatus)
            .addOnSuccessListener {
                val msg = if (isApproved) "User Approved" else "User Rejected"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                checkIfEmpty()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}
