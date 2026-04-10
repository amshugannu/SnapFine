package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ReviewQueueActivity : AppCompatActivity() {

    private lateinit var rvQueue: RecyclerView
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var adapter: ReviewQueueAdapter
    private val fStore = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null
    private var currentFilter = "pending" // Standardized initial state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_queue)

        initializeViews()
        setupListeners()
        fetchViolations(currentFilter)
    }

    private fun initializeViews() {
        rvQueue = findViewById(R.id.rvQueue)
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        rvQueue.layoutManager = LinearLayoutManager(this)
        adapter = ReviewQueueAdapter(emptyList()) { doc ->
            val intent = Intent(this, ReviewDetailActivity::class.java)
            intent.putExtra("violation_id", doc.id)
            startActivity(intent)
        }
        rvQueue.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { onBackPressed() }

        chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            val newFilter = when (checkedId) {
                R.id.chipPending -> "pending"
                R.id.chipApproved -> "approved"
                R.id.chipRejected -> "rejected"
                R.id.chipAll -> "all"
                else -> "pending"
            }
            
            if (newFilter != currentFilter) {
                currentFilter = newFilter
                fetchViolations(currentFilter)
            }
        }
    }

    private fun fetchViolations(status: String) {
        // Remove previous listener to avoid overlaps and data leaks
        snapshotListener?.remove()
        
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        
        var query: Query = fStore.collection("violations")
        
        if (status != "all") {
            query = query.whereEqualTo("status", status)
        }
        
        // Add sorting if needed (e.g., by date)
        // query = query.orderBy("timestamp", Query.Direction.DESCENDING)

        snapshotListener = query.addSnapshotListener { snapshot, e ->
            progressBar.visibility = View.GONE
            
            if (e != null) {
                tvEmptyState.text = "Error loading data"
                tvEmptyState.visibility = View.VISIBLE
                return@addSnapshotListener
            }

            val documents = snapshot?.documents ?: emptyList()
            adapter.updateList(documents)
            
            if (documents.isEmpty()) {
                tvEmptyState.text = getString(R.string.msg_no_reports)
                tvEmptyState.visibility = View.VISIBLE
                rvQueue.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                rvQueue.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}
