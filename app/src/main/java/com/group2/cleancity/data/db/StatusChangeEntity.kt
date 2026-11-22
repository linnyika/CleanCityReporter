package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.StatusChange

@Entity(tableName = "status_changes")
data class StatusChangeEntity(
    @PrimaryKey val id: Int,
    val reportId: String,
    val oldStatus: String?,
    val newStatus: String,
    val changedByUserId: Int,
    val changedByName: String,
    val timestamp: Long,
    val note: String?
) {
    fun toModel(): StatusChange = StatusChange(
        id = id,
        reportId = reportId,
        oldStatus = oldStatus?.let { ReportStatus.valueOf(it) },
        newStatus = ReportStatus.valueOf(newStatus),
        changedByUserId = changedByUserId,
        changedByName = changedByName,
        timestamp = timestamp,
        note = note
    )

    companion object {
        fun toEntity(statusChange: StatusChange): StatusChangeEntity = StatusChangeEntity(
            id = statusChange.id,
            reportId = statusChange.reportId,
            oldStatus = statusChange.oldStatus?.name,
            newStatus = statusChange.newStatus.name,
            changedByUserId = statusChange.changedByUserId,
            changedByName = statusChange.changedByName,
            timestamp = statusChange.timestamp,
            note = statusChange.note
        )
    }
}
