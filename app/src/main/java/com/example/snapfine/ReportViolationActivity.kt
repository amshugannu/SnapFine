package com.example.snapfine

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null
    private var selectedViolationType: String = ""
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_violation)

        initializeViews()
        setupSpinner()
        autoFillMetadata()
        setupDateTimePickers()
        loadPassedImage()

        btnSubmit.setOnClickListener {
            if (validateInputs() && imageUri != null) {
                startUploadProcess()
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
        
        // Dynamic Progress Bar setup if not in XML
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        progressBar.visibility = View.GONE
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
        updateDateLabel()
        updateTimeLabel()
        etLocation.setText("Indiranagar, Bangalore")
    }

    private fun setupDateTimePickers() {
        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateLabel()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeLabel()
        }

        TimePickerDialog(
            this,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateLabel() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(calendar.time))
    }

    private fun updateTimeLabel() {
        val myFormat = "hh:mm a"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etTime.setText(sdf.format(calendar.time))
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
            tilVehicleNumber.error = "Vehicle number required"
            isValid = false
        } else {
            tilVehicleNumber.error = null
        }

        if (selectedViolationType.isEmpty()) {
            Toast.makeText(this, "Violation type is required", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun startUploadProcess() {
        val uri = imageUri ?: return
        
        setLoadingState(true)

        CloudinaryHelper.uploadImageToCloudinary(uri, this) { secureUrl ->
            runOnUiThread {
                if (secureUrl != null) {
                    submitReportWithImageUrl(secureUrl)
                } else {
                    setLoadingState(false)
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnSubmit.isEnabled = !isLoading
        btnSubmit.text = if (isLoading) "Uploading Media..." else "Submit Report"
    }

    private fun submitReportWithImageUrl(cloudinaryUrl: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Standardized field mapping as per user requirement
        val violationData = hashMapOf(
            "vehicleNumber" to etVehicleNumber.text.toString().trim().uppercase(),
            "reportedBy" to currentUserUid,
            "imageUrl" to cloudinaryUrl,
            "status" to "pending",
            "type" to selectedViolationType, // user asked: type -> violation type
            "date" to etDate.text.toString(),
            "time" to etTime.text.toString(),
            "location" to etLocation.text.toString(),
            "description" to etDescription.text.toString(),
            "timestamp" to calendar.timeInMillis
        )

        FirebaseFirestore.getInstance().collection("violations")
            .add(violationData)
            .addOnSuccessListener {
                Snackbar.make(findViewById(android.R.id.content), "Report submitted successfully!", Snackbar.LENGTH_SHORT).show()
                ivViolationPreview.postDelayed({ finish() }, 1500)
            }
            .addOnFailureListener { e ->
                setLoadingState(false)
                Snackbar.make(findViewById(android.R.id.content), "Database error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}
