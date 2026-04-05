package com.example.snapfine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ViolationReviewActivity : AppCompatActivity() {

    private lateinit var rvViolationReview: RecyclerView
    private lateinit var tvEmptyViolations: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ViolationReviewAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_violation_review)

        // Safety access mapping
        if (UserSession.role != "staff") {
            Toast.makeText(this, "Access denied. Staff only.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvViolationReview = findViewById(R.id.rvViolationReview)
        tvEmptyViolations = findViewById(R.id.tvEmptyViolations)
        firestore = FirebaseFirestore.getInstance()

        rvViolationReview.layoutManager = LinearLayoutManager(this)

        fetchPendingViolations()
    }

    private fun fetchPendingViolations() {
        firestore.collection("violations")
            .whereEqualTo("status", "reported")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    tvEmptyViolations.visibility = View.VISIBLE
                    rvViolationReview.visibility = View.GONE
                } else {
                    tvEmptyViolations.visibility = View.GONE
                    rvViolationReview.visibility = View.VISIBLE

                    val violationsList = querySnapshot.documents.toMutableList()
                    adapter = ViolationReviewAdapter(violationsList) { doc, isApproved ->
                        processViolation(doc, isApproved)
                    }
                    rvViolationReview.adapter = adapter
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load reports.", Toast.LENGTH_SHORT).show()
                tvEmptyViolations.visibility = View.VISIBLE
                tvEmptyViolations.text = "Error connecting to servers"
                rvViolationReview.visibility = View.GONE
            }
    }

    private fun processViolation(doc: DocumentSnapshot, isApproved: Boolean) {
        val currentStaffUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val potentialOffender = doc.getString("potentialOffenderUID") ?: ""

        val updates = hashMapOf<String, Any>()

        if (isApproved) {
            updates["status"] = "approved"
            updates["approvedBy"] = currentStaffUid
            // LATE ASSIGNMENT ARCHITECTURE: Now physically assigns record so offender sees it
            updates["reportedToUID"] = potentialOffender
        } else {
            updates["status"] = "rejected"
            updates["rejectionReason"] = "Admin override"
        }

        firestore.collection("violations").document(doc.id)
            .update(updates)
            .addOnSuccessListener {
                val msg = if (isApproved) "Violation Approved & Assigned" else "Violation Rejected"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                checkIfEmpty()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update report", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            tvEmptyViolations.visibility = View.VISIBLE
            rvViolationReview.visibility = View.GONE
        }
    }
}
