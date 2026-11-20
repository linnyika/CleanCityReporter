package com.group2.cleancity.ui.alerts

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.group2.cleancity.databinding.ItemAlertBinding
import com.group2.cleancity.model.AppNotification

class AlertAdapter(
    private val onNotificationClick: (AppNotification) -> Unit
) : ListAdapter<AppNotification, AlertAdapter.AlertViewHolder>(NOTIFICATION_DIFF_CALLBACK) {

    fun submitNotifications(notifications: List<AppNotification>) {
        submitList(notifications.map { it.copy() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(
        private val binding: ItemAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) {
            binding.tvAlertMessage.text = notification.message
            binding.tvAlertTime.text = DateUtils.getRelativeTimeSpanString(
                notification.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.viewUnreadDot.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }

    private companion object {
        val NOTIFICATION_DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppNotification>() {
            override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
                return oldItem == newItem
            }
        }
    }
}

