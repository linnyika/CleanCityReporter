package com.group2.cleancity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: Int,
    val reporterName: String,
    val reporterContact: String,
    val categoryName: String,
    val description: String,
    val photoUri: String?,
    val locationText: String,
    val status: String,
    val priority: String,
    val assignedOfficerId: Int?,
    val assignedOfficerName: String?,
    val resolutionNotes: String?,
    val internalNotes: MutableList<String>,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toModel(): Report = Report(
        id = id,
        reporterId = reporterId,
        reporterName = reporterName,
        reporterContact = reporterContact,
        categoryName = categoryName,
        description = description,
        photoUri = photoUri,
        locationText = locationText,
        status = ReportStatus.valueOf(status),
        priority = Priority.valueOf(priority),
        assignedOfficerId = assignedOfficerId,
        assignedOfficerName = assignedOfficerName,
        resolutionNotes = resolutionNotes,
        internalNotes = internalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun toEntity(report: Report): ReportEntity = ReportEntity(
            id = report.id,
            reporterId = report.reporterId,
            reporterName = report.reporterName,
            reporterContact = report.reporterContact,
            categoryName = report.categoryName,
            description = report.description,
            photoUri = report.photoUri,
            locationText = report.locationText,
            status = report.status.name,
            priority = report.priority.name,
            assignedOfficerId = report.assignedOfficerId,
            assignedOfficerName = report.assignedOfficerName,
            resolutionNotes = report.resolutionNotes,
            internalNotes = report.internalNotes,
            createdAt = report.createdAt,
            updatedAt = report.updatedAt
        )
    }
}
