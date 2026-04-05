package com.example.snapfine

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateStaffAccountActivity : AppCompatActivity() {

    private lateinit var secondaryAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etStaffName: EditText
    private lateinit var etStaffEmail: EditText
    private lateinit var etStaffPhone: EditText
    private lateinit var etStaffPassword: EditText
    private lateinit var btnSubmitStaff: Button
    private lateinit var pbStaffCreation: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_staff_account)

        // Safety check
        if (UserSession.role != "admin") {
            Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etStaffName = findViewById(R.id.etStaffName)
        etStaffEmail = findViewById(R.id.etStaffEmail)
        etStaffPhone = findViewById(R.id.etStaffPhone)
        etStaffPassword = findViewById(R.id.etStaffPassword)
        btnSubmitStaff = findViewById(R.id.btnSubmitStaff)
        pbStaffCreation = findViewById(R.id.pbStaffCreation)
        firestore = FirebaseFirestore.getInstance()

        // Initialize secondary Firebase app to prevent logging out the current Admin
        var secondaryApp: FirebaseApp? = null
        try {
            secondaryApp = FirebaseApp.getInstance("SecondaryApp")
        } catch (e: IllegalStateException) {
            val options = FirebaseApp.getInstance().options
            secondaryApp = FirebaseApp.initializeApp(this, options, "SecondaryApp")
        }
        
        if (secondaryApp != null) {
            secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
        } else {
            Toast.makeText(this, "Critical error initializing auth layer", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnSubmitStaff.setOnClickListener {
            handleStaffCreation()
        }
    }

    private fun handleStaffCreation() {
        val name = etStaffName.text.toString().trim()
        val email = etStaffEmail.text.toString().trim()
        val phone = etStaffPhone.text.toString().trim()
        val password = etStaffPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            etStaffPassword.error = "Min 6 characters required"
            return
        }

        btnSubmitStaff.isEnabled = false
        pbStaffCreation.visibility = View.VISIBLE

        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newUserId = secondaryAuth.currentUser!!.uid
                    saveStaffToFirestore(newUserId, name, email, phone)
                } else {
                    btnSubmitStaff.isEnabled = true
                    pbStaffCreation.visibility = View.GONE
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveStaffToFirestore(uid: String, name: String, email: String, phone: String) {
        val staffUser = hashMapOf<String, Any>(
            "Name" to name,
            "email" to email,
            "phone" to phone,
            "role" to "staff",
            "verificationStatus" to "approved" // Staff skip pending queue implicitly
        )

        firestore.collection("users").document(uid).set(staffUser)
            .addOnSuccessListener {
                pbStaffCreation.visibility = View.GONE
                Toast.makeText(this, "Staff account created successfully", Toast.LENGTH_LONG).show()
                
                // Immediately sign out the secondary instance for security
                secondaryAuth.signOut()
                
                finish() // return to admin dashboard
            }
            .addOnFailureListener {
                btnSubmitStaff.isEnabled = true
                pbStaffCreation.visibility = View.GONE
                Toast.makeText(this, "Error saving staff data", Toast.LENGTH_SHORT).show()
                secondaryAuth.signOut()
            }
    }
}
