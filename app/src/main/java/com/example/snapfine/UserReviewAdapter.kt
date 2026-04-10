package com.example.snapfine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentSnapshot

class UserReviewAdapter(
    private val users: MutableList<DocumentSnapshot>,
    private val onAction: (DocumentSnapshot, Boolean) -> Unit
) : RecyclerView.Adapter<UserReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvVehicleDetails: TextView = view.findViewById(R.id.tvVehicleDetails)
        val btnApprove: MaterialButton = view.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userDoc = users[position]
        
        val name = userDoc.getString("name") ?: userDoc.getString("Name") ?: "Unknown"
        val email = userDoc.getString("email") ?: "No email"
        val phone = userDoc.getString("phone") ?: userDoc.getString("Phone") ?: "N/A"
        val license = userDoc.getString("licenseNumber") ?: "N/A"
        
        val vehicleNum = userDoc.getString("vehicleNumber") ?: userDoc.getString("VehicleNumber") ?: userDoc.getString("Vehicle Number") ?: "N/A"
        val vehicleType = userDoc.getString("vehicleType") ?: userDoc.getString("VehicleType") ?: userDoc.getString("Vehicle Type") ?: ""
        
        holder.tvUserName.text = name
        holder.tvUserEmail.text = email
        
        val details = StringBuilder()
        details.append("Phone: $phone\n")
        details.append("Vehicle: $vehicleNum")
        if (vehicleType.isNotEmpty()) details.append(" ($vehicleType)")
        details.append("\nLicense: $license")
        
        holder.tvVehicleDetails.text = details.toString()

        holder.btnApprove.setOnClickListener {
            onAction(userDoc, true)
        }

        holder.btnReject.setOnClickListener {
            onAction(userDoc, false)
        }
    }

    fun removeUser(docId: String) {
        val index = users.indexOfFirst { it.id == docId }
        if (index != -1) {
            users.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount() = users.size
}
