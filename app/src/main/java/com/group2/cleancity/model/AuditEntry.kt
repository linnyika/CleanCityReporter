package com.group2.cleancity.model

/**
 * Data class recording system-wide audit actions performed by officers or admins.
 */
data class AuditEntry(
    val id: Int, // Unique audit entry ID
    val action: String, // Action tag (e.g., "STATUS_UPDATE", "ASSIGN_OFFICER", "ADD_INTERNAL_NOTE")
    val details: String, // Textual description of action performed
    val performedByUserId: Int, // User ID of officer or admin
    val performedByName: String, // Full name of officer or admin
    val timestamp: Long = System.currentTimeMillis() // Timestamp of action
)
