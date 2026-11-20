package com.group2.cleancity.data

import com.group2.cleancity.model.AppNotification

object NotificationRepository {
    private val notifications = mutableListOf<AppNotification>()
    private var nextId = 1

    fun add(userId: Int, reportId: String, message: String): AppNotification {
        val notification = AppNotification(
            id = nextId++,
            userId = userId,
            reportId = reportId,
            message = message
        )
        notifications.add(notification)
        return notification
    }

    fun getForUser(userId: Int): List<AppNotification> {
        return notifications
            .filter { it.userId == userId }
            .sortedByDescending { it.timestamp }
    }

    fun getUnposted(userId: Int): List<AppNotification> {
        return notifications
            .filter { it.userId == userId && !it.isPosted }
            .sortedBy { it.timestamp }
    }

    fun unreadCount(userId: Int): Int {
        return notifications.count { it.userId == userId && !it.isRead }
    }

    fun markAsRead(id: Int) {
        notifications.find { it.id == id }?.isRead = true
    }

    fun markPosted(id: Int) {
        notifications.find { it.id == id }?.isPosted = true
    }

    fun markAllAsRead(userId: Int) {
        notifications
            .filter { it.userId == userId }
            .forEach { it.isRead = true }
    }
}
