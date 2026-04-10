package com.example.snapfine

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snapfine.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvHeaderName: TextView
    private lateinit var tvHeaderEmail: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvVehicleNumber: TextView
    private lateinit var tvVehicleType: TextView
    private lateinit var tvRegistrationId: TextView
    private lateinit var tvLicenseNumber: TextView
    private lateinit var btnEdit: View

    private val fAuth = FirebaseAuth.getInstance()
    private val fStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvHeaderName = findViewById(R.id.tvHeaderName)
        tvHeaderEmail = findViewById(R.id.tvHeaderEmail)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvVehicleNumber = findViewById(R.id.tvVehicleNumber)
        tvVehicleType = findViewById(R.id.tvVehicleType)
        tvRegistrationId = findViewById(R.id.tvRegistrationId)
        tvLicenseNumber = findViewById(R.id.tvLicenseNumber)
        btnEdit = findViewById(R.id.btnEditProfile)
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnEdit.applyScaleAnimation()
        btnEdit.setOnClickListener {
            startActivity(android.content.Intent(this, EditProfileActivity::class.java))
        }

        val btnLogout = findViewById<View>(R.id.btnLogout)
        btnLogout.applyScaleAnimation()
        btnLogout.setOnClickListener {
            fAuth.signOut()
            val intent = android.content.Intent(this, LoginActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val userId = fAuth.currentUser?.uid ?: return
        
        // Baseline email from Auth
        tvHeaderEmail.text = fAuth.currentUser?.email
        tvEmail.text = fAuth.currentUser?.email

        fStore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        // Exhaustive manual fallbacks to support all historical key variations (spaces, PascalCase, and camelCase)
                        if (it.name == null) it.name = document.getString("Name") ?: document.getString("name")
                        
                        if (it.vehicleNumber == null) {
                            it.vehicleNumber = document.getString("vehicleNumber") 
                                ?: document.getString("VehicleNumber") 
                                ?: document.getString("Vehicle Number")
                        }
                        
                        if (it.vehicleType == null) {
                            it.vehicleType = document.getString("vehicleType") 
                                ?: document.getString("VehicleType") 
                                ?: document.getString("Vehicle Type")
                        }
                        
                        if (it.vehicleRegistrationId == null) {
                            it.vehicleRegistrationId = document.getString("vehicleRegistrationId") 
                                ?: document.getString("VehicleRegistrationId") 
                                ?: document.getString("Vehicle Registration Id")
                        }
                        
                        bindUserData(it)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bindUserData(user: User) {
        tvHeaderName.text = user.name ?: "User"
        tvFullName.text = user.name ?: "Not provided"
        tvPhone.text = user.phone ?: "Not provided"
        
        tvVehicleNumber.text = user.vehicleNumber ?: "Not linked"
        tvVehicleType.text = user.vehicleType ?: "N/A"
        tvRegistrationId.text = user.vehicleRegistrationId ?: "N/A"
        tvLicenseNumber.text = user.licenseNumber ?: "N/A"

        // Hide Edit button if verified
        if (user.verificationStatus.equals("approved", ignoreCase = true)) {
            btnEdit.visibility = View.GONE
        } else {
            btnEdit.visibility = View.VISIBLE
        }

        // Update header email if firestore has a different one
        user.email?.let {
            tvHeaderEmail.text = it
            tvEmail.text = it
        }
    }
}
