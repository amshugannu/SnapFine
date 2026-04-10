package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcomeUser: TextView
    private lateinit var tvReportsCount: TextView
    private lateinit var tvPendingFines: TextView
    
    private val fAuth = FirebaseAuth.getInstance()
    private val fStore = FirebaseFirestore.getInstance()
    
    private var reportsListener: ListenerRegistration? = null
    private var finesListener: ListenerRegistration? = null
    private var notifListener: ListenerRegistration? = null

    private lateinit var tvNotifBadge: TextView
    private lateinit var layoutNotif: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        initializeViews()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        tvReportsCount = findViewById(R.id.tvReportsCount)
        tvPendingFines = findViewById(R.id.tvPendingFines)
        tvNotifBadge = findViewById(R.id.tvNotificationBadge)
        layoutNotif = findViewById(R.id.layoutNotifications)

        val complaintsCard = findViewById<View>(R.id.card_my_complaints)
        val violationsCard = findViewById<View>(R.id.card_my_violations)

        // Standardize Activity Cards content
        complaintsCard.findViewById<TextView>(R.id.tvActionTitle).text = "My Complaints"
        complaintsCard.findViewById<TextView>(R.id.tvActionSubtitle).text = "Track submitted reports"
        complaintsCard.findViewById<ImageView>(R.id.ivActionIcon).setImageResource(R.drawable.baseline_edit_24)

        violationsCard.findViewById<TextView>(R.id.tvActionTitle).text = "My Violations"
        violationsCard.findViewById<TextView>(R.id.tvActionSubtitle).text = "View fines and status"
        violationsCard.findViewById<ImageView>(R.id.ivActionIcon).setImageResource(R.drawable.violations_img)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val intent = Intent(this, ReportViolationActivity::class.java)
            intent.putExtra("imageUri", uri.toString())
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.ivProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        layoutNotif.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // Admin Management Actions
        findViewById<View>(R.id.card_review_violations).setOnClickListener {
            startActivity(Intent(this, ReviewQueueActivity::class.java))
        }
        findViewById<View>(R.id.card_review_users).setOnClickListener {
            startActivity(Intent(this, UserReviewActivity::class.java))
        }
        findViewById<View>(R.id.card_add_staff).setOnClickListener {
            startActivity(Intent(this, CreateStaffAccountActivity::class.java))
        }

        // Standard Quick Actions (Citizen features for Admin)
        findViewById<View>(R.id.card_camera_report).setOnClickListener {
            if (UserSession.isEligibleToReport()) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                Toast.makeText(this, "u are still not eligiblie for the reporting violation", Toast.LENGTH_LONG).show()
            }
        }
        findViewById<View>(R.id.card_gallery_report).setOnClickListener {
            if (UserSession.isEligibleToReport()) {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                Toast.makeText(this, "u are still not eligiblie for the reporting violation", Toast.LENGTH_LONG).show()
            }
        }

        // Standard Activity Tracking
        findViewById<View>(R.id.card_my_complaints).setOnClickListener {
            startActivity(Intent(this, MyComplaintsActivity::class.java))
        }
        findViewById<View>(R.id.card_my_violations).setOnClickListener {
            startActivity(Intent(this, MyCasesActivity::class.java))
        }
    }

    private fun loadUserData() {
        val userId = fAuth.currentUser?.uid ?: return
        
        fStore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: doc.getString("Name") ?: "Administrator"
                    tvWelcomeUser.text = "Hi, $name 👋"
                    
                    val role = doc.getString("role") ?: "admin"
                    val status = doc.getString("verificationStatus") ?: "approved"
                    UserSession.updateSession(role, status)
                }
            }

        // Real-time metrics for current user
        reportsListener = fStore.collection("violations")
            .whereEqualTo("reportedBy", userId)
            .addSnapshotListener { snapshot, _ ->
                tvReportsCount.text = snapshot?.size()?.toString() ?: "0"
            }

        finesListener = fStore.collection("violations")
            .whereEqualTo("violatorUid", userId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    var totalFine = 0
                    for (doc in snapshot.documents) {
                        val fineStr = doc.getString("fineAmount") ?: "0"
                        val numericFine = fineStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                        totalFine += numericFine
                    }
                    tvPendingFines.text = "₹$totalFine"
                }
            }

        notifListener = fStore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                if (count > 0) {
                    tvNotifBadge.text = count.toString()
                    tvNotifBadge.visibility = View.VISIBLE
                } else {
                    tvNotifBadge.visibility = View.GONE
                }
            }
    }

    override fun onDestroy() {
        reportsListener?.remove()
        finesListener?.remove()
        notifListener?.remove()
        super.onDestroy()
    }
}
