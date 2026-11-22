package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.AuditEntry

@Entity(tableName = "audit_entries")
data class AuditEntryEntity(
    @PrimaryKey val id: Int,
    val action: String,
    val details: String,
    val performedByUserId: Int,
    val performedByName: String,
    val timestamp: Long
) {
    fun toModel(): AuditEntry = AuditEntry(
        id = id,
        action = action,
        details = details,
        performedByUserId = performedByUserId,
        performedByName = performedByName,
        timestamp = timestamp
    )

    companion object {
        fun toEntity(entry: AuditEntry): AuditEntryEntity = AuditEntryEntity(
            id = entry.id,
            action = entry.action,
            details = entry.details,
            performedByUserId = entry.performedByUserId,
            performedByName = entry.performedByName,
            timestamp = entry.timestamp
        )
    }
}
