package com.example.snapfine

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyCasesActivity : AppCompatActivity() {

    private lateinit var rvMyViolations: RecyclerView
    private lateinit var adapter: ViolationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: View
    private lateinit var btnBack: ImageButton

    private val fStore = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cases)

        initializeViews()
        loadViolations()
    }

    private fun initializeViews() {
        rvMyViolations = findViewById(R.id.rvMyViolations)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnBack = findViewById(R.id.btnBack)

        rvMyViolations.layoutManager = LinearLayoutManager(this)
        adapter = ViolationAdapter()
        rvMyViolations.adapter = adapter

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadViolations() {
        val currentUserId = fAuth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE

        // Removed orderBy on server to avoid requiring a composite index
        fStore.collection("violations")
            .whereEqualTo("violatorUid", currentUserId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                
                if (snapshot.isEmpty) {
                    layoutEmpty.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                } else {
                    layoutEmpty.visibility = View.GONE
                    val violationsList = mutableListOf<Violation>()
                    for (doc in snapshot.documents) {
                        try {
                            // Manual mapping to handle legacy/new field variety
                            val violation = Violation(
                                vehicleNumber = doc.getString("vehicleNumber") ?: "",
                                reportedBy = doc.getString("reportedBy") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: doc.getString("evidenceUrl") ?: "",
                                type = doc.getString("type") ?: doc.getString("violationType") ?: "Traffic Violation",
                                location = doc.getString("location") ?: "",
                                description = doc.getString("description") ?: "",
                                fineAmount = doc.getString("fineAmount") ?: "0",
                                status = doc.getString("status") ?: "approved",
                                time = doc.getString("time") ?: "",
                                date = doc.getString("date") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                approvedBy = doc.getString("approvedBy") ?: "",
                                rejectionReason = doc.getString("rejectionReason") ?: "",
                                violatorUid = doc.getString("violatorUid") ?: ""
                            )
                            violationsList.add(violation)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    // Sort by timestamp descending in Kotlin for compatibility
                    violationsList.sortByDescending { it.timestamp }
                    adapter.updateData(violationsList)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load violations: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
