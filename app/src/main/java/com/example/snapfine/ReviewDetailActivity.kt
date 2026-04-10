package com.example.snapfine

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewDetailActivity : AppCompatActivity() {

    private lateinit var ivEvidence: ImageView
    private lateinit var tvDetailType: TextView
    private lateinit var chipStatus: TextView
    private lateinit var tvVehicleNumber: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tilFineAmount: TextInputLayout
    private lateinit var etFineAmount: TextInputEditText
    private lateinit var btnApprove: MaterialButton
    private lateinit var btnReject: MaterialButton
    private lateinit var fineInputSection: View
    private lateinit var actionLayout: View
    private lateinit var btnBack: ImageButton

    private val fStore = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()
    private var violationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)

        violationId = intent.getStringExtra("violation_id") ?: run {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        loadViolationData()
    }

    private fun initializeViews() {
        ivEvidence = findViewById(R.id.ivEvidence)
        tvDetailType = findViewById(R.id.tvDetailType)
        chipStatus = findViewById(R.id.chipStatus)
        tvVehicleNumber = findViewById(R.id.tvVehicleNumber)
        tvLocation = findViewById(R.id.tvLocation)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvDescription = findViewById(R.id.tvDescription)
        
        tilFineAmount = findViewById(R.id.tilFineAmount)
        etFineAmount = findViewById(R.id.etFineAmount)
        fineInputSection = findViewById(R.id.fineInputSection)
        
        btnApprove = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)
        actionLayout = findViewById(R.id.actionLayout)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { onBackPressed() }

        btnApprove.setOnClickListener {
            validateAndApprove()
        }

        btnReject.setOnClickListener {
            processDecision(false, "0")
        }
    }

    private fun loadViolationData() {
        fStore.collection("violations").document(violationId!!).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val status = doc.getString("status") ?: "pending"
                    
                    tvDetailType.text = doc.getString("type") ?: doc.getString("violationType") ?: "Unknown"
                    chipStatus.text = status.uppercase()
                    
                    tvVehicleNumber.text = doc.getString("vehicleNumber")
                    tvLocation.text = doc.getString("location")
                    tvDateTime.text = "${doc.getString("date")}, ${doc.getString("time")}"
                    tvDescription.text = doc.getString("description") ?: "No description provided."

                    // Style status chip
                    when (status.lowercase()) {
                        "pending" -> chipStatus.setBackgroundResource(R.drawable.chip_warning)
                        "approved" -> chipStatus.setBackgroundResource(R.drawable.chip_success)
                        "rejected" -> chipStatus.setBackgroundResource(R.drawable.chip_error)
                    }

                    // Standardized Glide binding for Cloudinary images
                    val imageUrl = doc.getString("imageUrl") ?: doc.getString("evidenceUrl") ?: ""
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .fitCenter()
                            .into(ivEvidence)
                    }

                    // Only show actions if pending
                    if (status == "pending") {
                        actionLayout.visibility = View.VISIBLE
                        fineInputSection.visibility = View.VISIBLE
                    } else {
                        actionLayout.visibility = View.GONE
                        fineInputSection.visibility = View.GONE
                    }
                }
            }
    }

    private fun validateAndApprove() {
        val amount = etFineAmount.text.toString().trim()
        if (amount.isEmpty()) {
            tilFineAmount.error = "Fine amount is required for approval"
            return
        }
        tilFineAmount.error = null
        processDecision(true, amount)
    }

    private fun processDecision(isApproved: Boolean, fineAmount: String) {
        val updates = mutableMapOf<String, Any>()
        val currentUser = fAuth.currentUser?.uid ?: ""

        btnApprove.isEnabled = false
        btnReject.isEnabled = false

        if (isApproved) {
            updates["status"] = "approved"
            updates["fineAmount"] = fineAmount
            updates["approvedBy"] = currentUser
            
            val vehicleNum = tvVehicleNumber.text.toString().uppercase()
            
            fStore.collection("vehicles").document(vehicleNum).get()
                .addOnSuccessListener { vDoc ->
                    if (vDoc != null && vDoc.exists()) {
                        val violatorUid = vDoc.getString("uid")
                        if (violatorUid != null) {
                            updates["violatorUid"] = violatorUid
                        }
                    }
                    finalizeUpdate(updates, "Violation Approved")
                }
                .addOnFailureListener { finalizeUpdate(updates, "Violation Approved") }
        } else {
            updates["status"] = "rejected"
            updates["rejectedBy"] = currentUser
            finalizeUpdate(updates, "Violation Rejected")
        }
    }

    private fun finalizeUpdate(updates: Map<String, Any>, message: String) {
        val violationType = tvDetailType.text.toString()
        val fineAmount = updates["fineAmount"] as? String ?: "0"
        val violatorUid = updates["violatorUid"] as? String

        fStore.collection("violations").document(violationId!!)
            .update(updates)
            .addOnSuccessListener {
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
                
                // Create notification if approved
                if (updates["status"] == "approved" && violatorUid != null) {
                    sendInAppNotification(violatorUid, violationType, fineAmount)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1000)
            }
            .addOnFailureListener {
                btnApprove.isEnabled = true
                btnReject.isEnabled = true
                Snackbar.make(findViewById(android.R.id.content), "Update failed", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun sendInAppNotification(violatorUid: String, type: String, fine: String) {
        val notification = Notification(
            userId = violatorUid,
            title = "Violation Approved \uD83D\uDEA8",
            message = "Your violation has been approved. Fine: ₹$fine",
            type = "violation",
            timestamp = System.currentTimeMillis(),
            read = false
        )

        fStore.collection("notifications").add(notification)
            .addOnSuccessListener {
                // Now fetch the FCM token to trigger a push notification
                fStore.collection("users").document(violatorUid).get()
                    .addOnSuccessListener { userDoc ->
                        val token = userDoc.getString("fcmToken")
                        if (token != null) {
                            android.util.Log.d("FCM_TRIGGER", "Sending push to token: $token")
                            // TODO: Trigger FCM v1 API call via Cloud Function or Backend
                            // For now, this token can be used in the Firebase Console
                        }
                    }
            }
    }
}
