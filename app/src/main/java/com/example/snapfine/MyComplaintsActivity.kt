package com.example.snapfine

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyComplaintsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComplaintsAdapter
    private lateinit var emptyStateView: View
    private lateinit var loadingStateView: View
    private lateinit var backbtn: ImageButton
    private var registration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        recyclerView = findViewById(R.id.rvMyComplaints)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        // Enable smooth item animations for expand/collapse
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = true

        emptyStateView = findViewById(R.id.layoutEmpty)
        val progressBar = findViewById<View>(R.id.progressBar)

        backbtn = findViewById(R.id.btnBack)
        backbtn.applyScaleAnimation()
        backbtn.setOnClickListener { onBackPressed() }

        adapter = ComplaintsAdapter()
        recyclerView.adapter = adapter

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(this, "Please login to view your complaints.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show Initial Loading
        progressBar.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        registration = db.collection("violations")
            .whereEqualTo("reportedBy", currentUserUid)
            .addSnapshotListener { snapshots, e ->
                // Hide Loading once first response arrives
                progressBar.visibility = View.GONE

                if (e != null) {
                    Toast.makeText(this, "Failed to sync reports: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val complaints = snapshots.toObjects(Violation::class.java)
                    adapter.updateData(complaints)
                    
                    if (complaints.isEmpty()) {
                        emptyStateView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyStateView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
    }
}
