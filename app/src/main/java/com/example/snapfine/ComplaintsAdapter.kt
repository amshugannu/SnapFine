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

        holder.tvViolationType.text = complaint.violationType
        holder.tvVehicleNumber.text = "Vehicle: ${complaint.vehicleNumber}"
        holder.tvDateTime.text = "${complaint.date} • ${complaint.time}"
        
        // Status Handling
        val status = complaint.status.lowercase(Locale.ROOT)
        when {
            status.contains("approved") || status.contains("fine_issued") -> {
                holder.tvStatusChip.text = "APPROVED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_success)
            }
            status.contains("rejected") -> {
                holder.tvStatusChip.text = "REJECTED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_error)
            }
            else -> {
                holder.tvStatusChip.text = "PENDING"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_warning)
            }
        }

        // Image Handling
        val imageUrl = if (complaint.evidenceUrl.isNotEmpty()) complaint.evidenceUrl else complaint.imageUrl
        if (imageUrl.isNotEmpty()) {
            holder.ivThumbnail.visibility = if (isExpanded) View.GONE else View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivThumbnail)
            
            if (isExpanded) {
                holder.ivFullPreview.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .into(holder.ivFullPreview)
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.baseline_edit_24) // Placeholder
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
            if (isExpanded) notifyItemChanged(position) // Ensure current item updates if collapsing
        }
    }
}
