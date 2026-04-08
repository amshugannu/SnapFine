package com.example.snapfine

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var adapter: ViolationAdapter
    private lateinit var emptyView: TextView
    private lateinit var backbtn: ImageButton
    private var registration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        recyclerView = findViewById(R.id.recyclerViewMyComplaints)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recyclerView.itemAnimator = null
        emptyView = findViewById(R.id.emptyView)

        backbtn = findViewById(R.id.btn_back)
        backbtn.setOnClickListener { onBackPressed() }

        adapter = ViolationAdapter()
        recyclerView.adapter = adapter

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(this, "Please login to view your complaints.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Dashboard Diagnosis: Temporarily remove orderBy to bypass index requirement
        Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] --- REPORTER QUERY DIAGNOSIS ---")
        Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] Current User UID: $currentUserUid")
        
        val db = FirebaseFirestore.getInstance()
        registration = db.collection("violations")
            .whereEqualTo("reportedBy", currentUserUid)
            // .orderBy("timestamp", Query.Direction.DESCENDING) // TEMPORARILY REMOVED FOR INDEX TESTING
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("SnapFine-DEBUG", "[DEBUG-REPORTER] Listen failed. Error: ${e.message}", e)
                    // SHOW ERROR ON UI so user can see 'Index Required' or other errors
                    Toast.makeText(this, "Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] Snapshot received. Document count: ${snapshots.size()}")
                    
                    val rawDocsInfo = snapshots.documents.map { doc ->
                        "ID: ${doc.id}, reportedBy: ${doc.getString("reportedBy")}"
                    }.joinToString("\n")
                    Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] Raw Documents:\n$rawDocsInfo")

                    val complaints = snapshots.toObjects(Violation::class.java)
                    Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] Step 5 & 9 Check: Fetched ${complaints.size} objects for reporter $currentUserUid")
                    
                    // Step 7: Force UI Refresh
                    adapter.updateData(complaints)
                    
                    if (complaints.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Log.d("SnapFine-DEBUG", "[DEBUG-REPORTER] Snapshot is NULL")
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
