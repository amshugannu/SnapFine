package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViolationAdapter(options: FirestoreRecyclerOptions<Violation>) :
    FirestoreRecyclerAdapter<Violation, ViolationAdapter.ViolationViewHolder>(options) {

    class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val violationTypeTextView: TextView = itemView.findViewById(R.id.violationTypeTextView)
        val vehicleNumberTextView: TextView = itemView.findViewById(R.id.vehicleNumberTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val fineAmountTextView: TextView = itemView.findViewById(R.id.fineAmountTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        // Add any additional views if needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_violation, parent, false) // Use your existing item XML layout
        return ViolationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int, model: Violation) {
        holder.violationTypeTextView.text = "Violation: ${model.violationType}"
        holder.vehicleNumberTextView.text = "Vehicle: ${model.vehicleNumber}"
        holder.locationTextView.text = "Location: ${model.location}"
        holder.dateTextView.text = "Date: ${model.date}"
        holder.timeTextView.text = "Time: ${model.time}"
        holder.descriptionTextView.text = "Description: ${model.description}"
        holder.fineAmountTextView.text = "Fine: ₹${model.fineAmount}"
        holder.statusTextView.text = "Status: ${model.status}"
        // You can customize this adapter further as per your need

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val violationType = model.violationType?.trim()
        val formattedTimestamp = if (model.timestamp != 0L)
            dateFormat.format(Date(model.timestamp))
        else
            "N/A"
        holder.timestampTextView.text = "Reported at: $formattedTimestamp"

        val finesCollection = FirebaseFirestore.getInstance().collection("fines")
        finesCollection.document("fineid") // Replace with your actual document ID
            .get()
            .addOnSuccessListener { document ->
                if (!violationType.isNullOrEmpty()) {
                    try {
                        val fineValue = document.get(violationType)
                        val fineAmount = fineValue?.toString() ?: "Not available"
                        holder.fineAmountTextView.text = "Fine: ₹$fineAmount"
                    } catch (e: IllegalArgumentException) {
                        android.util.Log.e("ViolationAdapter", "Invalid field path: $violationType", e)
                        holder.fineAmountTextView.text = "Fine: Not available"
                    }
                } else {
                    android.util.Log.e("ViolationAdapter", "Empty or null violationType for model: $model")
                    holder.fineAmountTextView.text = "Fine: Not available"
                }
            }
            .addOnFailureListener { exception ->
                holder.fineAmountTextView.text = "Error: ${exception.message}"
            }
    }

}