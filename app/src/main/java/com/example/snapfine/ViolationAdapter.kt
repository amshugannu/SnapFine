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
        val btnPayNow: MaterialButton = itemView.findViewById(R.id.btnPayNow)
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
        
        holder.tvViolationType.text = violation.violationType
        holder.tvVehicleNumber.text = "Vehicle: ${violation.vehicleNumber}"
        holder.tvDate.text = violation.date
        
        val fineAmount = violation.fineAmount
        holder.tvFineAmount.text = if (fineAmount.isNullOrEmpty() || fineAmount == "0") "₹ Pending" else "₹$fineAmount"
        
        // Status Handling
        val status = violation.status.lowercase(Locale.ROOT)
        val isPaid = status.contains("paid")
        
        if (isPaid) {
            holder.tvStatusChip.text = "PAID"
            holder.tvStatusChip.setBackgroundResource(R.drawable.chip_paid)
            
            holder.btnPayNow.text = "Paid"
            holder.btnPayNow.isEnabled = false
            holder.btnPayNow.setBackgroundColor(android.graphics.Color.LTGRAY)
            holder.btnPayNow.setTextColor(android.graphics.Color.WHITE)
        } else {
            holder.tvStatusChip.text = "UNPAID"
            holder.tvStatusChip.setBackgroundResource(R.drawable.chip_unpaid)
            
            holder.btnPayNow.text = "Pay Now"
            holder.btnPayNow.isEnabled = true
            // Reset to default style colors (managed by theme/material button)
            holder.btnPayNow.setBackgroundColor(holder.itemView.context.getColor(R.color.colorPrimary))
            holder.btnPayNow.setTextColor(android.graphics.Color.WHITE)
        }

        // Image Handling
        val imageUrl = if (violation.evidenceUrl.isNotEmpty()) violation.evidenceUrl else violation.imageUrl
        if (imageUrl.isNotEmpty()) {
            holder.ivThumbnail.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.visibility = View.GONE
        }
    }
}
