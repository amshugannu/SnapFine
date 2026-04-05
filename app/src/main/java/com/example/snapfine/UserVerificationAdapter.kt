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
        
        val name = userDoc.getString("Name") ?: "Unknown User"
        val email = userDoc.getString("email") ?: "No email"
        val phone = userDoc.getString("phone") ?: "No phone"
        
        holder.tvUserName.text = name
        holder.tvUserDetails.text = "Email: $email\nPhone: $phone"

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
