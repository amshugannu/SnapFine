package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide

class ViolationAdapter(private var violations: MutableList<Violation> = mutableListOf()) :
    RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    fun updateData(newList: List<Violation>) {
        violations.clear()
        violations.addAll(newList)
        notifyDataSetChanged()
    }

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
        val violationEvidenceImage: ImageView = itemView.findViewById(R.id.violationEvidenceImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_violation, parent, false)
        return ViolationViewHolder(view)
    }

    override fun getItemCount(): Int = violations.size

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val model = violations[position]
        Log.d("ViolationAdapter", "Binding violation at pos $position: ${model.vehicleNumber}, status: ${model.status}")
        
        holder.violationTypeTextView.text = "Violation: ${model.violationType}"
        holder.vehicleNumberTextView.text = "Vehicle: ${model.vehicleNumber}"
        holder.locationTextView.text = "Location: ${model.location}"
        holder.dateTextView.text = "Date: ${model.date}"
        holder.timeTextView.text = "Time: ${model.time}"
        holder.descriptionTextView.text = "Description: ${model.description}"
        holder.fineAmountTextView.text = "Fine: ₹${model.fineAmount}"
        
        val statusStr = model.status ?: ""
        holder.statusTextView.text = "Status: $statusStr"
        val statusLower = statusStr.lowercase(Locale.ROOT)
        when {
            statusLower.contains("approved") || statusLower.contains("fine_issued") -> 
                holder.statusTextView.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            statusLower.contains("reported") || statusLower.contains("pending") -> 
                holder.statusTextView.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            statusLower.contains("rejected") -> 
                holder.statusTextView.setTextColor(android.graphics.Color.parseColor("#F44336"))
            else -> 
                holder.statusTextView.setTextColor(android.graphics.Color.BLACK)
        }

        // Load evidence image
        val finalImageUrl = if (model.evidenceUrl.isNotEmpty()) model.evidenceUrl else model.imageUrl
        if (finalImageUrl.isNotEmpty()) {
            holder.violationEvidenceImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(finalImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.violationEvidenceImage)
        } else {
            holder.violationEvidenceImage.visibility = View.GONE
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val violationType = model.violationType?.trim()
        val formattedTimestamp = if (model.timestamp != 0L)
            dateFormat.format(Date(model.timestamp))
        else
            "N/A"
        holder.timestampTextView.text = "Reported at: $formattedTimestamp"

        // Display assigned fine if available, otherwise fetch from catalog
        if (!model.fineAmount.isNullOrEmpty() && model.fineAmount != "0") {
            holder.fineAmountTextView.text = "Fine: ₹${model.fineAmount}"
        } else {
            val finesCollection = FirebaseFirestore.getInstance().collection("fines")
            finesCollection.document("fineid") 
                .get()
                .addOnSuccessListener { document ->
                    if (!violationType.isNullOrEmpty()) {
                        try {
                            val fineValue = document.get(violationType)
                            val fineAmountValue = fineValue?.toString() ?: "Pending"
                            holder.fineAmountTextView.text = "Fine: ₹$fineAmountValue"
                        } catch (e: Exception) {
                            holder.fineAmountTextView.text = "Fine: Pending"
                        }
                    } else {
                        holder.fineAmountTextView.text = "Fine: Pending"
                    }
                }
                .addOnFailureListener {
                    holder.fineAmountTextView.text = "Fine: Pending"
                }
        }
    }
}
