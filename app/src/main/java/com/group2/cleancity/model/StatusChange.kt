package com.group2.cleancity.model

/**
 * Data class tracking an individual status transition in a report's lifecycle history.
 */
data class StatusChange(
    val id: Int, // Unique status change ID
    val reportId: String, // ID of report being updated (e.g. "NRB-0001")
    val oldStatus: ReportStatus?, // Previous status (null for initial creation)
    val newStatus: ReportStatus, // New status after transition
    val changedByUserId: Int, // User ID of officer/admin who changed status
    val changedByName: String, // Full name of officer/admin
    val timestamp: Long = System.currentTimeMillis(), // Timestamp of transition
    val note: String? = null // Optional note or mandatory resolution details
)
