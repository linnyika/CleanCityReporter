package com.group2.cleancity.data

import com.group2.cleancity.model.AuditEntry

/**
 * In-memory repository for storing and retrieving system-wide audit records.
 */
object AuditRepository {

    // Internal mutable list holding audit entries
    private val entries = mutableListOf<AuditEntry>()
    private var nextId = 1

    // Logs a new administrative or officer action into the audit trail
    fun log(action: String, details: String, userId: Int, userName: String) {
        entries.add(
            AuditEntry(
                id = nextId++,
                action = action,
                details = details,
                performedByUserId = userId,
                performedByName = userName,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Returns all audit entries ordered by timestamp (newest first)
    fun getAll(): List<AuditEntry> = entries.toList().sortedByDescending { it.timestamp }
}
