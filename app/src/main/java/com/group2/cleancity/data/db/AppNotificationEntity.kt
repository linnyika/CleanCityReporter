package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.AppNotification

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val reportId: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isPosted: Boolean
) {
    fun toModel(): AppNotification = AppNotification(
        id = id,
        userId = userId,
        reportId = reportId,
        message = message,
        timestamp = timestamp,
        isRead = isRead,
        isPosted = isPosted
    )

    companion object {
        fun toEntity(notification: AppNotification): AppNotificationEntity = AppNotificationEntity(
            id = notification.id,
            userId = notification.userId,
            reportId = notification.reportId,
            message = notification.message,
            timestamp = notification.timestamp,
            isRead = notification.isRead,
            isPosted = notification.isPosted
        )
    }
}
