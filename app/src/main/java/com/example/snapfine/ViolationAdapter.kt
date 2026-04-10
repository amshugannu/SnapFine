package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snapfine.Violation
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ViolationAdapter(private var violations: MutableList<Violation> = mutableListOf()) :
    RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    fun updateData(newList: List<Violation>) {
        violations.clear()
        violations.addAll(newList)
        notifyDataSetChanged()
    }

    class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvViolationType: TextView = itemView.findViewById(R.id.tvViolationType)
        val tvStatusChip: TextView = itemView.findViewById(R.id.tvStatusChip)
        val tvVehicleNumber: TextView = itemView.findViewById(R.id.tvVehicleNumber)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvFineAmount: TextView = itemView.findViewById(R.id.tvFineAmount)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_violation, parent, false)
        return ViolationViewHolder(view)
    }

    override fun getItemCount(): Int = violations.size

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val violation = violations[position]
        
        // Dynamic fallback for type display
        holder.tvViolationType.text = if (violation.type.isNotEmpty()) violation.type else "Traffic Violation"
        holder.tvVehicleNumber.text = "Vehicle: ${violation.vehicleNumber}"
        holder.tvDate.text = violation.date
        
        val fineAmount = violation.fineAmount
        holder.tvFineAmount.text = if (fineAmount.isNullOrEmpty() || fineAmount == "0") "₹ Pending" else "₹$fineAmount"
        
        // Status Handling (Strict 3-Status System)
        val status = violation.status.lowercase(Locale.ROOT)
        
        when (status) {
            "pending" -> {
                holder.tvStatusChip.text = "PENDING"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_warning)
            }
            "approved" -> {
                holder.tvStatusChip.text = "APPROVED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_success)
            }
            "rejected" -> {
                holder.tvStatusChip.text = "REJECTED"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_error)
            }
            else -> {
                holder.tvStatusChip.text = "UNKNOWN"
                holder.tvStatusChip.setBackgroundResource(R.drawable.chip_warning)
            }
        }

        // Image Handling (Standardized Cloudinary URL Display)
        val imageUrl = violation.imageUrl
        if (imageUrl.isNotEmpty()) {
            holder.ivThumbnail.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.visibility = View.GONE
        }
    }
}
