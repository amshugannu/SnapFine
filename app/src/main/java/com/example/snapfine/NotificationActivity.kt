package com.example.snapfine

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton

    private lateinit var adapter: NotificationAdapter
    private val fStore = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        initializeViews()
        setupRecyclerView()
        listenForNotifications()
    }

    private fun initializeViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            markAsRead(notification)
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun listenForNotifications() {
        val currentUserUid = fAuth.currentUser?.uid ?: return
        
        progressBar.visibility = View.VISIBLE

        fStore.collection("notifications")
            .whereEqualTo("userId", currentUserUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE
                
                if (e != null) {
                    Toast.makeText(this, "Error fetching notifications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val notifList = snapshots.toObjects(Notification::class.java)
                    adapter.updateList(notifList)
                    
                    if (notifList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        rvNotifications.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        rvNotifications.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun markAsRead(notification: Notification) {
        if (notification.read) return // Already read

        fStore.collection("notifications").document(notification.id)
            .update("read", true)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update notification status", Toast.LENGTH_SHORT).show()
            }
    }
}
