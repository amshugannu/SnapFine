package com.example.snapfine

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserReviewActivity : AppCompatActivity() {

    private lateinit var rvUserReview: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var adapter: UserReviewAdapter
    private val fStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_review)

        initializeViews()
        fetchPendingUsers()
    }

    private fun initializeViews() {
        rvUserReview = findViewById(R.id.rvUserReview)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        rvUserReview.layoutManager = LinearLayoutManager(this)
        btnBack.setOnClickListener { finish() }
    }

    private fun fetchPendingUsers() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        // STEP 1: Query citizens with pending verification
        fStore.collection("users")
            .whereEqualTo("role", "citizen")
            .whereEqualTo("verificationStatus", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                val documents = snapshot.documents.toMutableList()
                
                if (documents.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvUserReview.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvUserReview.visibility = View.VISIBLE
                    adapter = UserReviewAdapter(documents) { doc, isApproved ->
                        if (isApproved) approveUser(doc) else rejectUser(doc)
                    }
                    rvUserReview.adapter = adapter
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveUser(doc: DocumentSnapshot) {
        // STEP 4: Approve Logic
        fStore.collection("users").document(doc.id)
            .update(mapOf(
                "isVerified" to true,
                "verificationStatus" to "approved"
            ))
            .addOnSuccessListener {
                showFeedback("User Approved")
                adapter.removeUser(doc.id)
                checkIfEmpty()
            }
    }

    private fun rejectUser(doc: DocumentSnapshot) {
        // STEP 5: Reject Logic
        fStore.collection("users").document(doc.id)
            .update(mapOf(
                "isVerified" to false,
                "verificationStatus" to "rejected"
            ))
            .addOnSuccessListener {
                showFeedback("User Registration Rejected")
                adapter.removeUser(doc.id)
                checkIfEmpty()
            }
    }

    private fun showFeedback(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            tvEmptyState.visibility = View.VISIBLE
            rvUserReview.visibility = View.GONE
        }
    }
}
