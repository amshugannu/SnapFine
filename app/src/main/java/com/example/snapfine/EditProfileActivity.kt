package com.example.snapfine

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etVehicleNumber: EditText
    private lateinit var etVehicleType: EditText
    private lateinit var etRegistrationId: EditText
    private lateinit var etLicenseNumber: EditText

    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilVehicleNumber: TextInputLayout

    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageButton

    private val fAuth = FirebaseAuth.getInstance()
    private val fStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        loadUserData()

        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveUserData()
            }
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etVehicleNumber = findViewById(R.id.etVehicleNumber)
        etVehicleType = findViewById(R.id.etVehicleType)
        etRegistrationId = findViewById(R.id.etRegistrationId)
        etLicenseNumber = findViewById(R.id.etLicenseNumber)

        tilFullName = findViewById(R.id.tilFullName)
        tilPhone = findViewById(R.id.tilPhone)
        tilVehicleNumber = findViewById(R.id.tilVehicleNumber)

        btnSave = findViewById(R.id.btnSaveChanges)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadUserData() {
        val userId = fAuth.currentUser?.uid ?: return
        etEmail.setText(fAuth.currentUser?.email)

        fStore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    user?.let {
                        // Exhaustive manual fallbacks for loading legacy data (including keys with spaces)
                        val firstName = it.name ?: doc.getString("Name") ?: doc.getString("name")
                        val vNum = it.vehicleNumber ?: doc.getString("VehicleNumber") ?: doc.getString("Vehicle Number")
                        val vType = it.vehicleType ?: doc.getString("VehicleType") ?: doc.getString("Vehicle Type")
                        val vReg = it.vehicleRegistrationId ?: doc.getString("VehicleRegistrationId") ?: doc.getString("Vehicle Registration Id")

                        etFullName.setText(firstName)
                        etPhone.setText(it.phone)
                        etVehicleNumber.setText(vNum)
                        etVehicleType.setText(vType)
                        etRegistrationId.setText(vReg)
                        etLicenseNumber.setText(it.licenseNumber)
                    }
                }
            }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = etFullName.text.toString().trim()
        if (name.isEmpty()) {
            tilFullName.error = getString(R.string.error_required)
            isValid = false
        } else {
            tilFullName.error = null
        }

        val phone = etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            tilPhone.error = getString(R.string.error_required)
            isValid = false
        } else {
            tilPhone.error = null
        }

        val vehicleNum = etVehicleNumber.text.toString().trim()
        if (vehicleNum.isEmpty()) {
            tilVehicleNumber.error = getString(R.string.error_required)
            isValid = false
        } else {
            tilVehicleNumber.error = null
        }

        return isValid
    }

    private fun saveUserData() {
        val userId = fAuth.currentUser?.uid ?: return
        
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val userData = hashMapOf(
            "name" to etFullName.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "vehicleNumber" to etVehicleNumber.text.toString().trim().uppercase(),
            "vehicleType" to etVehicleType.text.toString().trim(),
            "vehicleRegistrationId" to etRegistrationId.text.toString().trim(),
            "licenseNumber" to etLicenseNumber.text.toString().trim()
        )

        fStore.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                showSuccessSnackbar(getString(R.string.msg_profile_updated))
                // Update vehicle mapping if number changed
                updateVehicleMapping(userId, userData["VehicleNumber"] as String, userData["phone"] as String)
                
                etFullName.postDelayed({ finish() }, 1500)
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                btnSave.text = getString(R.string.btn_save_changes)
                showErrorSnackbar("Update failed: ${e.message}")
            }
    }

    private fun updateVehicleMapping(uid: String, vehicleNum: String, phone: String) {
        if (vehicleNum.isEmpty()) return
        val mapping = hashMapOf("uid" to uid, "phone" to phone)
        fStore.collection("vehicles").document(vehicleNum).set(mapping, SetOptions.merge())
    }
}
