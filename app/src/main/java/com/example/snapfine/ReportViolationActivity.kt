package com.example.snapfine

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReportViolationActivity : AppCompatActivity() {

    private lateinit var ivViolationPreview: ImageView
    private lateinit var etVehicleNumber: EditText
    private lateinit var tilVehicleNumber: TextInputLayout
    private lateinit var etLocation: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerViolationType: Spinner
    private lateinit var btnSubmit: MaterialButton

    // Hidden logic fields (retained for data mapping)
    private lateinit var etPhoneNumber: EditText
    private lateinit var etUidInput: EditText

    private var imageUri: Uri? = null
    private var selectedViolationType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_violation)

        initializeViews()
        setupSpinner()
        autoFillMetadata()
        loadPassedImage()

        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                submitReport()
            }
        }
    }

    private fun initializeViews() {
        ivViolationPreview = findViewById(R.id.violationImageView)
        etVehicleNumber = findViewById(R.id.vehicleNumberInput)
        tilVehicleNumber = findViewById(R.id.tilVehicleNumber)
        etLocation = findViewById(R.id.locationInput)
        etDate = findViewById(R.id.dateInput)
        etTime = findViewById(R.id.timeInput)
        etDescription = findViewById(R.id.violationDescription)
        spinnerViolationType = findViewById(R.id.violationTypeSpinner)
        btnSubmit = findViewById(R.id.uploadButton)

        // Map hidden fields to avoid crashes if original logic relies on them
        etPhoneNumber = findViewById(R.id.phoneNumberInput)
        etUidInput = findViewById(R.id.uidInput)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.violation_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerViolationType.adapter = adapter

        spinnerViolationType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedViolationType = if (position == 0) "" else parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun autoFillMetadata() {
        val calendar = Calendar.getInstance()
        
        val dateSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(dateSdf.format(calendar.time))

        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        etTime.setText(timeSdf.format(calendar.time))

        // Auto-fill location with a placeholder (in real app, use FusedLocationProvider)
        etLocation.setText("Indiranagar, Bangalore")
    }

    private fun loadPassedImage() {
        val uriString = intent.getStringExtra("IMAGE_URI") ?: intent.getStringExtra("imageUri")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            ivViolationPreview.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "No image received", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val vehicleNumber = etVehicleNumber.text.toString().trim()
        if (vehicleNumber.isEmpty()) {
            tilVehicleNumber.error = getString(R.string.error_required)
            isValid = false
        } else {
            tilVehicleNumber.error = null
        }

        if (selectedViolationType.isEmpty()) {
            Toast.makeText(this, getString(R.string.label_violation_type) + " is required", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun submitReport() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Show Loading State
        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        val violationData = hashMapOf(
            "vehicleNumber" to etVehicleNumber.text.toString().trim().uppercase(),
            "reportedBy" to currentUserUid,
            "evidenceUrl" to (imageUri?.toString() ?: ""),
            "date" to etDate.text.toString(),
            "time" to etTime.text.toString(),
            "location" to etLocation.text.toString(),
            "violationType" to selectedViolationType,
            "description" to etDescription.text.toString(),
            "timestamp" to System.currentTimeMillis(),
            "status" to "reported",
            "violatorUid" to "" // This would normally come from vehicle search logic
        )

        FirebaseFirestore.getInstance().collection("violations")
            .add(violationData)
            .addOnSuccessListener {
                showSuccessSnackbar("Report submitted successfully!")
                // Return to home or history after short delay
                ivViolationPreview.postDelayed({ finish() }, 1500)
            }
            .addOnFailureListener { e ->
                btnSubmit.isEnabled = true
                btnSubmit.text = getString(R.string.btn_submit_report)
                showErrorSnackbar("Submission failed: ${e.message}")
            }
    }
}
