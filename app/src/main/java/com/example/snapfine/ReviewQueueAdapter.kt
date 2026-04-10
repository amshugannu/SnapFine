package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot

class ReviewQueueAdapter(
    private var violations: List<DocumentSnapshot>,
    private val onItemClick: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<ReviewQueueAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvViolationType: TextView = view.findViewById(R.id.tvViolationType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvVehicleNumber: TextView = view.findViewById(R.id.tvVehicleNumber)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = violations[position]
        
        // Standardized field mapping for Cloudinary transition (with Legacy Fallbacks)
        val type = doc.getString("type") ?: doc.getString("violationType") ?: "Unknown"
        val status = doc.getString("status") ?: "pending"
        val vehicle = doc.getString("vehicleNumber") ?: "N/A"
        val time = doc.getString("time") ?: ""
        val date = doc.getString("date") ?: ""
        val imageUrl = doc.getString("imageUrl") ?: doc.getString("evidenceUrl") ?: ""

        holder.tvViolationType.text = type
        holder.tvVehicleNumber.text = vehicle
        holder.tvTimestamp.text = "$date, $time"

        // Status Chip styling
        holder.tvStatus.text = status.uppercase()
        when (status.lowercase()) {
            "pending" -> holder.tvStatus.setBackgroundResource(R.drawable.chip_warning)
            "approved" -> holder.tvStatus.setBackgroundResource(R.drawable.chip_success)
            "rejected" -> holder.tvStatus.setBackgroundResource(R.drawable.chip_error)
            else -> holder.tvStatus.setBackgroundResource(R.drawable.chip_warning)
        }

        if (imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        holder.itemView.setOnClickListener { onItemClick(doc) }
    }

    override fun getItemCount() = violations.size

    fun updateList(newList: List<DocumentSnapshot>) {
        violations = newList
        notifyDataSetChanged()
    }
}
