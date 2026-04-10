package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot

class UserVerificationAdapter(
    private val users: MutableList<DocumentSnapshot>,
    private val onActionComplete: (DocumentSnapshot, Boolean) -> Unit
) : RecyclerView.Adapter<UserVerificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserDetails: TextView = view.findViewById(R.id.tvUserDetails)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_verification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userDoc = users[position]
        val roleStr = userDoc.getString("role")

        if (roleStr != null && !roleStr.equals("citizen", ignoreCase = true)) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        val name = userDoc.getString("name") ?: userDoc.getString("Name") ?: "Unknown User"
        val email = userDoc.getString("email") ?: "No email"
        val phone = userDoc.getString("phone") ?: "No phone"
        val vehicle = userDoc.getString("vehicleNumber") ?: userDoc.getString("VehicleNumber") ?: userDoc.getString("Vehicle Number") ?: "N/A"
        val vType = userDoc.getString("vehicleType") ?: userDoc.getString("VehicleType") ?: userDoc.getString("Vehicle Type") ?: ""
        
        holder.tvUserName.text = name
        val details = if (vType.isNotEmpty()) {
            "Email: $email\nPhone: $phone\nVehicle: $vehicle ($vType)"
        } else {
            "Email: $email\nPhone: $phone\nVehicle: $vehicle"
        }
        holder.tvUserDetails.text = details

        holder.btnApprove.setOnClickListener {
            onActionComplete(userDoc, true)
            removeUserSafely(userDoc)
        }

        holder.btnReject.setOnClickListener {
            onActionComplete(userDoc, false)
            removeUserSafely(userDoc)
        }
    }

    private fun removeUserSafely(userDoc: DocumentSnapshot) {
        val index = users.indexOf(userDoc)
        if (index != -1) {
            users.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount() = users.size
}
