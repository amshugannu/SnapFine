package com.example.snapfine

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotifClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardNotification)
        val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
        val ivIcon: ImageView = view.findViewById(R.id.ivNotifIcon)
        val viewUnread: View = view.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notifications[position]

        holder.tvTitle.text = notif.title
        holder.tvMessage.text = notif.message
        
        val timeAgo = DateUtils.getRelativeTimeSpanString(
            notif.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvTime.text = timeAgo

        // UI for read/unread
        if (notif.read) {
            holder.viewUnread.visibility = View.GONE
            holder.card.alpha = 0.8f
        } else {
            holder.viewUnread.visibility = View.VISIBLE
            holder.card.alpha = 1.0f
        }

        holder.card.setOnClickListener {
            onNotifClick(notif)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateList(newList: List<Notification>) {
        notifications = newList
        notifyDataSetChanged()
    }
}
