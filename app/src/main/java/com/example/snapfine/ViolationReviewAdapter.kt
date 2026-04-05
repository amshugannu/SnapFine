package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViolationReviewAdapter(
    private val violations: MutableList<DocumentSnapshot>,
    private val onActionComplete: (DocumentSnapshot, Boolean) -> Unit
) : RecyclerView.Adapter<ViolationReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVehicleNumber: TextView = view.findViewById(R.id.tvVehicleNumber)
        val tvViolationType: TextView = view.findViewById(R.id.tvViolationType)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val btnApproveViolation: Button = view.findViewById(R.id.btnApproveViolation)
        val btnRejectViolation: Button = view.findViewById(R.id.btnRejectViolation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_violation_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = violations[position]

        val vehicleNum = doc.getString("vehicleNumber") ?: "Unknown Vehicle"
        val vType = doc.getString("violationType") ?: "Unknown Violation"
        val tvstamp = doc.getLong("timestamp") ?: 0L
        
        holder.tvVehicleNumber.text = "Vehicle: $vehicleNum"
        holder.tvViolationType.text = "Type: $vType"
        
        if (tvstamp != 0L) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            holder.tvTimestamp.text = "Reported at: ${dateFormat.format(Date(tvstamp))}"
        } else {
            holder.tvTimestamp.text = "Reported at: N/A"
        }

        holder.btnApproveViolation.setOnClickListener {
            onActionComplete(doc, true)
            removeSafely(doc)
        }

        holder.btnRejectViolation.setOnClickListener {
            onActionComplete(doc, false)
            removeSafely(doc)
        }
    }

    private fun removeSafely(doc: DocumentSnapshot) {
        val index = violations.indexOf(doc)
        if (index != -1) {
            violations.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount() = violations.size
}
