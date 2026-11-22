package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.AuditEntryDao
import com.group2.cleancity.data.db.AuditEntryEntity
import com.group2.cleancity.model.AuditEntry

/**
 * Singleton repository for storing and retrieving system-wide audit records.
 */
object AuditRepository {

    private var auditDao: AuditEntryDao? = null
    private val fallbackEntries = mutableListOf<AuditEntry>()
    private var fallbackNextId = 1

    fun init(context: Context) {
        if (auditDao == null) {
            val db = AppDatabase.getInstance(context)
            auditDao = db.auditDao()
        }
    }

    // Logs a new administrative or officer action into the audit trail
    fun log(action: String, details: String, userId: Int, userName: String) {
        val dao = auditDao
        if (dao != null) {
            val all = dao.getAll()
            val nextId = (all.maxOfOrNull { it.id } ?: 0) + 1
            dao.insert(
                AuditEntryEntity(
                    id = nextId,
                    action = action,
                    details = details,
                    performedByUserId = userId,
                    performedByName = userName,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            fallbackEntries.add(
                AuditEntry(
                    id = fallbackNextId++,
                    action = action,
                    details = details,
                    performedByUserId = userId,
                    performedByName = userName,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Returns all audit entries ordered by timestamp (newest first)
    fun getAll(): List<AuditEntry> {
        val dao = auditDao
        if (dao != null) {
            return dao.getAll().map { it.toModel() }
        }
        return fallbackEntries.toList().sortedByDescending { it.timestamp }
    }
}
