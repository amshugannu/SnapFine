package com.example.snapfine

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import androidx.core.text.HtmlCompat
import androidx.core.widget.NestedScrollView
import java.util.*

class ReportViolationActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var vehicleNumberInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var uidInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var descriptionInput: EditText
    private lateinit var timeInput: TextInputEditText
    private lateinit var dateInput: TextInputEditText
    private lateinit var uploadButton: Button
    private lateinit var violationTypeSpinner: Spinner
    private lateinit var loadingDialog: ProgressDialog
    private lateinit var locationInput: EditText
    private lateinit var scrollView: NestedScrollView


    private var selectedViolationType: String = ""
    private var imageUri: Uri? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_violation)

        // Initialize views
        imageView = findViewById(R.id.violationImageView)
        vehicleNumberInput = findViewById(R.id.vehicleNumberInput)
        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        uidInput = findViewById(R.id.uidInput)
        searchButton = findViewById(R.id.searchButton)
        descriptionInput = findViewById(R.id.violationDescription)
        timeInput = findViewById(R.id.timeInput)
        dateInput = findViewById(R.id.dateInput)
        uploadButton = findViewById(R.id.uploadButton)
        violationTypeSpinner = findViewById(R.id.violationTypeSpinner)
        locationInput = findViewById(R.id.locationInput)
        scrollView = findViewById(R.id.scrollView)


        setupViolationTypeSpinner()

        loadingDialog = ProgressDialog(this).apply {
            setMessage("Submitting report...")
            setCancelable(false)
        }

        setupDateAndTimePickers()

        val reportTitleTextView = findViewById<TextView>(R.id.reportTitle2)
        val coloredText = "Traffic <font color='#FF0000'>Violation</font>"
        reportTitleTextView.text = HtmlCompat.fromHtml(coloredText, HtmlCompat.FROM_HTML_MODE_LEGACY)


        // Load Image
        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "No image received.", Toast.LENGTH_SHORT).show()
            finish()
        }

        searchButton.setOnClickListener {
            val vehicleNumber = vehicleNumberInput.text.toString().trim()
            if (vehicleNumber.isNotEmpty()) {
                fetchVehicleOwnerData(vehicleNumber)
            } else {
                Toast.makeText(this, "Enter vehicle number", Toast.LENGTH_SHORT).show()
            }
        }
        uploadButton.setOnClickListener {
            uploadViolationReport()
        }

        locationInput.setOnClickListener {
            scrollToView(locationInput)
        }

        descriptionInput.setOnClickListener {
            scrollToView(descriptionInput)
        }

    }
    private fun scrollToView(view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.bottom)
        }
    }


    private fun setupDateAndTimePickers() {
        val calendar = Calendar.getInstance()

        dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateInput.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeInput.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    timeInput.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun setupViolationTypeSpinner() {
        val types = listOf(
            "Select Violation Type",
            "Signal Jumping",
            "No Helmet",
            "Wrong Parking",
            "Overspeeding",
            "Driving Without License",
            "Triple Riding",
            "Hit and Run",
            "Distracted Driving",
            "Rash Driving"
        )

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.custom_spinner_item,
            types
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the hint item from being selected
                return position != 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view as TextView
                tv.setTextColor(resources.getColor(R.color.black))
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                tv.setTextColor(
                    if (position == 0) resources.getColor(R.color.grey)
                    else resources.getColor(R.color.black)
                )
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        violationTypeSpinner.adapter = adapter
        violationTypeSpinner.setSelection(0)

        violationTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedViolationType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchVehicleOwnerData(vehicleNumber: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("vehicles")
            .document(vehicleNumber.uppercase())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val phoneNumber = document.getString("phone") ?: ""
                    val uid = document.getString("uid") ?: ""
                    phoneNumberInput.setText(phoneNumber)
                    uidInput.setText(uid)
                } else {
                    Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadViolationReport() {
        val vehicleNumber = vehicleNumberInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        val reportedToUID = uidInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val time = timeInput.text.toString().trim()
        val date = dateInput.text.toString().trim()
        val location = locationInput.text.toString().trim()

        if (vehicleNumber.isEmpty() || description.isEmpty() || time.isEmpty() ||
            date.isEmpty() || selectedViolationType == "Select Violation Type" || location.isEmpty()) {
            Toast.makeText(this, "Please fill all details and select a valid violation type.", Toast.LENGTH_SHORT).show()
            return
        }

        val reportedByUID = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        uploadButton.isEnabled = false
        uploadButton.text = "Submitting..."
        loadingDialog.show()

        val violationData = hashMapOf(
            "vehicleNumber" to vehicleNumber,
            "reportedByUID" to reportedByUID,
            "reportedToUID" to reportedToUID,
            "imageUrl" to "https://dummyimage.com/600x400/000/fff&text=No+Image",
            "date" to date,
            "time" to time,
            "location" to location,
            "violationType" to selectedViolationType,
            "description" to description,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Pending"
        )

        FirebaseFirestore.getInstance()
            .collection("violations")
            .add(violationData)
            .addOnSuccessListener {
                sendNotificationToVehicleOwner(reportedToUID, vehicleNumber)
                loadingDialog.dismiss()
                Toast.makeText(this, "Violation submitted successfully.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to submit: ${e.message}", Toast.LENGTH_SHORT).show()
                resetUploadButton()
            }
    }

    private fun sendNotificationToVehicleOwner(reportedToUID: String, vehicleNumber: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(reportedToUID).get()
            .addOnSuccessListener { document ->
                val token = document.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    val notificationData = mapOf(
                        "to" to token,
                        "notification" to mapOf(
                            "title" to "New Traffic Violation",
                            "body" to "A new fine has been issued for vehicle $vehicleNumber."
                        )
                    )
                    sendFCMNotification(notificationData)
                }
            }
    }

    private fun sendFCMNotification(notificationData: Map<String, Any>) {
        println("Send this notification from your backend: $notificationData")
    }

    private fun resetUploadButton() {
        uploadButton.isEnabled = true
        uploadButton.text = "Upload"
    }
}
