package com.group2.cleancity.model

data class AppNotification(
    val id: Int,
    val userId: Int,
    val reportId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    var isPosted: Boolean = false
)
