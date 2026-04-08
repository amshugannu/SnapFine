package com.example.snapfine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class ViolationReviewActivity : AppCompatActivity() {

    private lateinit var rvViolationReview: RecyclerView
    private lateinit var tvEmptyViolations: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ViolationReviewAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_violation_review)

        // Safety access mapping
        if (UserSession.role != "staff") {
            Toast.makeText(this, "Access denied. Staff only.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvViolationReview = findViewById(R.id.rvViolationReview)
        tvEmptyViolations = findViewById(R.id.tvEmptyViolations)
        firestore = FirebaseFirestore.getInstance()

        rvViolationReview.layoutManager = LinearLayoutManager(this)

        fetchPendingViolations()
    }

    private fun fetchPendingViolations() {
        firestore.collection("violations")
            .whereEqualTo("status", "reported")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("ViolationReview", "Fetched ${querySnapshot.size()} reports")
                if (querySnapshot.isEmpty) {
                    tvEmptyViolations.visibility = View.VISIBLE
                    rvViolationReview.visibility = View.GONE
                } else {
                    tvEmptyViolations.visibility = View.GONE
                    rvViolationReview.visibility = View.VISIBLE

                    val violationsList = querySnapshot.documents.toMutableList()
                    adapter = ViolationReviewAdapter(violationsList) { doc, isApproved ->
                        processViolation(doc, isApproved)
                    }
                    rvViolationReview.adapter = adapter
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load reports.", Toast.LENGTH_SHORT).show()
                tvEmptyViolations.visibility = View.VISIBLE
                tvEmptyViolations.text = "Error connecting to servers"
                rvViolationReview.visibility = View.GONE
            }
    }

    private fun processViolation(doc: DocumentSnapshot, isApproved: Boolean) {
        if (isApproved) {
            showFineAmountDialog(doc)
        } else {
            finalizeViolationUpdate(doc, false, "0")
        }
    }

    private fun showFineAmountDialog(doc: DocumentSnapshot) {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "500"

        android.app.AlertDialog.Builder(this)
            .setTitle("Confirm Approval")
            .setMessage("Please enter the fine amount for this violation:")
            .setView(input)
            .setPositiveButton("Approve") { _, _ ->
                val fineValue = input.text.toString().ifEmpty { "0" }
                finalizeViolationUpdate(doc, true, fineValue)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Refresh list if cancelled to bring item back since adapter removes it optimistically
                fetchPendingViolations()
            }
            .show()
    }

    private fun finalizeViolationUpdate(doc: DocumentSnapshot, isApproved: Boolean, fineAmount: String) {
        val currentStaffUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val vehicleNumber = doc.getString("vehicleNumber") ?: ""
        
        Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] --- APPROVAL START (Re-verifying Owner) ---")
        Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] Violation Document ID: ${doc.id}")
        Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] Vehicle Number from Doc: $vehicleNumber")
        Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] Processing Approved: $isApproved, Amount: $fineAmount")

        if (isApproved && vehicleNumber.isNotEmpty()) {
            firestore.collection("vehicles").document(vehicleNumber.uppercase())
                .get()
                .addOnSuccessListener { vehicleDoc ->
                    val ownerUid = vehicleDoc.getString("uid")
                    Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] Re-fetched ownerUID from 'vehicles' is: $ownerUid")

                    if (ownerUid == null) {
                        Log.e("SnapFine-DEBUG", "[DEBUG-APPROVE] CRITICAL ERROR: ownerUID is NULL for vehicle $vehicleNumber. STOPPING.")
                        Toast.makeText(this, "Error: Could not verify vehicle owner. Aborting approval.", Toast.LENGTH_LONG).show()
                        fetchPendingViolations()
                        return@addOnSuccessListener
                    }

                    // Proceed with update using the verified ownerUid
                    performFirestoreUpdate(doc.id, isApproved, fineAmount, ownerUid, currentStaffUid)
                }
                .addOnFailureListener { e ->
                    Log.e("SnapFine-DEBUG", "[DEBUG-APPROVE] Failed to fetch vehicle owner during approval", e)
                    Toast.makeText(this, "Error fetching vehicle owner info.", Toast.LENGTH_SHORT).show()
                    fetchPendingViolations()
                }
        } else {
            // Rejection or missing vehicle number (rejected case doesn't need violatorUid)
            performFirestoreUpdate(doc.id, isApproved, fineAmount, null, currentStaffUid)
        }
    }

    private fun performFirestoreUpdate(docId: String, isApproved: Boolean, fineAmount: String, ownerUid: String?, staffUid: String) {
        val updates = hashMapOf<String, Any>()

        if (isApproved) {
            updates["status"] = "approved"
            updates["approvedBy"] = staffUid
            updates["fineAmount"] = fineAmount
            if (ownerUid != null) {
                updates["violatorUid"] = ownerUid
                updates["reportedToUID"] = ownerUid // Legacy field support
            }
        } else {
            updates["status"] = "rejected"
            updates["rejectionReason"] = "Staff override"
        }

        firestore.collection("violations").document(docId)
            .update(updates)
            .addOnSuccessListener {
                val msg = if (isApproved) "Violation Approved and Assigned to $ownerUid" else "Violation Rejected"
                Log.d("SnapFine-DEBUG", "[DEBUG-APPROVE] Final update success: $msg")
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                checkIfEmpty()
            }
            .addOnFailureListener { e ->
                Log.e("SnapFine-DEBUG", "[DEBUG-APPROVE] Final update failed", e)
                Toast.makeText(this, "Failed to update report", Toast.LENGTH_SHORT).show()
                fetchPendingViolations()
            }
    }

    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            tvEmptyViolations.visibility = View.VISIBLE
            rvViolationReview.visibility = View.GONE
        }
    }
}
