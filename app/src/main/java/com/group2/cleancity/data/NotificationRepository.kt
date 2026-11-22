package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.AppNotificationDao
import com.group2.cleancity.data.db.AppNotificationEntity
import com.group2.cleancity.model.AppNotification

object NotificationRepository {

    private var notificationDao: AppNotificationDao? = null
    private val fallbackNotifications = mutableListOf<AppNotification>()
    private var fallbackNextId = 1

    fun init(context: Context) {
        if (notificationDao == null) {
            val db = AppDatabase.getInstance(context)
            notificationDao = db.notificationDao()
        }
    }

    fun add(userId: Int, reportId: String, message: String): AppNotification {
        val dao = notificationDao
        val notification = if (dao != null) {
            val all = dao.getAll()
            val nextId = (all.maxOfOrNull { it.id } ?: 0) + 1
            val n = AppNotification(
                id = nextId,
                userId = userId,
                reportId = reportId,
                message = message
            )
            dao.insert(AppNotificationEntity.toEntity(n))
            n
        } else {
            val n = AppNotification(
                id = fallbackNextId++,
                userId = userId,
                reportId = reportId,
                message = message
            )
            fallbackNotifications.add(n)
            n
        }
        return notification
    }

    fun getForUser(userId: Int): List<AppNotification> {
        val dao = notificationDao
        if (dao != null) {
            return dao.getForUser(userId).map { it.toModel() }
        }
        return fallbackNotifications
            .filter { it.userId == userId }
            .sortedByDescending { it.timestamp }
    }

    fun getUnposted(userId: Int): List<AppNotification> {
        val dao = notificationDao
        if (dao != null) {
            return dao.getUnposted(userId).map { it.toModel() }
        }
        return fallbackNotifications
            .filter { it.userId == userId && !it.isPosted }
            .sortedBy { it.timestamp }
    }

    fun unreadCount(userId: Int): Int {
        val dao = notificationDao
        if (dao != null) {
            return dao.unreadCount(userId)
        }
        return fallbackNotifications.count { it.userId == userId && !it.isRead }
    }

    fun markAsRead(id: Int) {
        val dao = notificationDao
        if (dao != null) {
            val entity = dao.getById(id)
            if (entity != null) {
                val updated = entity.toModel().apply { isRead = true }
                dao.update(AppNotificationEntity.toEntity(updated))
            }
        } else {
            fallbackNotifications.find { it.id == id }?.isRead = true
        }
    }

    fun markPosted(id: Int) {
        val dao = notificationDao
        if (dao != null) {
            val entity = dao.getById(id)
            if (entity != null) {
                val updated = entity.toModel().apply { isPosted = true }
                dao.update(AppNotificationEntity.toEntity(updated))
            }
        } else {
            fallbackNotifications.find { it.id == id }?.isPosted = true
        }
    }

    fun markAllAsRead(userId: Int) {
        val dao = notificationDao
        if (dao != null) {
            val list = dao.getForUser(userId)
            for (entity in list) {
                if (!entity.isRead) {
                    val updated = entity.toModel().apply { isRead = true }
                    dao.update(AppNotificationEntity.toEntity(updated))
                }
            }
        } else {
            fallbackNotifications
                .filter { it.userId == userId }
                .forEach { it.isRead = true }
        }
    }
}
