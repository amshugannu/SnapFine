package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapfine.Violation
import java.util.Locale

class ComplaintsAdapter(private var complaints: MutableList<Violation> = mutableListOf()) :
    RecyclerView.Adapter<ComplaintsAdapter.ComplaintViewHolder>() {

    private var expandedPosition: Int = -1

    fun updateData(newList: List<Violation>) {
        complaints.clear()
        complaints.addAll(newList)
        notifyDataSetChanged()
    }

    class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvViolationType: TextView = itemView.findViewById(R.id.tvViolationType)
        val tvStatusChip: TextView = itemView.findViewById(R.id.tvStatusChip)
        val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val ivExpandArrow: ImageView = itemView.findViewById(R.id.ivExpandArrow)
        
        // Expandable section
        val layoutDetails: View = itemView.findViewById(R.id.layoutDetails)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val ivFullPreview: ImageView = itemView.findViewById(R.id.ivFullPreview)
        val cardComplaint: View = itemView.findViewById(R.id.cardComplaint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_complaint, parent, false)
        return ComplaintViewHolder(view)
    }

    override fun getItemCount(): Int = complaints.size

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaints[position]
        val isExpanded = position == expandedPosition

        val type = if (complaint.type.isNotEmpty()) complaint.type else "Traffic Violation"
        holder.tvViolationType.text = type
        holder.tvVehicleNumber.text = "Vehicle: ${complaint.vehicleNumber}"
        holder.tvDateTime.text = "${complaint.date} • ${complaint.time}"
        
        // Status Handling
        // Status Handling (Strict 3-Status System)
        val status = complaint.status.lowercase(Locale.ROOT)
        when (status) {
            "approved" -> {
                holder.tvStatusChip.text = "APPROVED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_success)
            }
            "rejected" -> {
                holder.tvStatusChip.text = "REJECTED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_error)
            }
            else -> { // Default to PENDING
                holder.tvStatusChip.text = "PENDING"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_warning)
            }
        }

        // Image Handling (Standardized Cloudinary display with Legacy fallback)
        val imageUrl = if (complaint.imageUrl.isNotEmpty()) complaint.imageUrl else "" 
        // Note: For existing objects, mapping happens in the Activity/Fragment for Violation objects
        
        if (imageUrl.isNotEmpty()) {
            holder.ivThumbnail.visibility = if (isExpanded) View.GONE else View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivThumbnail)
            
            if (isExpanded) {
                holder.ivFullPreview.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivFullPreview)
            }
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_report_image)
            holder.ivThumbnail.visibility = if (isExpanded) View.GONE else View.VISIBLE
            holder.ivFullPreview.visibility = View.GONE
        }

        // Expand/Collapse Logic
        holder.layoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpandArrow.rotation = if (isExpanded) 180f else 0f

        holder.tvLocation.text = if (complaint.location.isNotEmpty()) complaint.location else "Not specified"
        holder.tvDescription.text = if (complaint.description.isNotEmpty()) complaint.description else "No description provided"

        holder.cardComplaint.setOnClickListener {
            val prevExpanded = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            
            // Smooth transition
            TransitionManager.beginDelayedTransition(holder.itemView.parent as ViewGroup, AutoTransition())
            
            if (prevExpanded != -1) notifyItemChanged(prevExpanded)
            if (expandedPosition != -1) notifyItemChanged(expandedPosition)
            if (isExpanded) notifyItemChanged(position)
        }
    }
}
