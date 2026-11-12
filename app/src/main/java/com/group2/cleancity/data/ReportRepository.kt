package com.group2.cleancity.data

import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.StatusChange
import java.util.Date

object ReportRepository{
    private val reports = mutableListOf<Report>()
    private val statusHistory = mutableListOf<StatusChange>()
    private var nextReportNumber = 1
    var forceSaveFailure = false
    fun getAllReports(): List<Report>{
        return reports.toList()
    }
    fun getReportById(reportId: String): Report? {
        return reports.find {it.id == reportId}
    }
    fun getStatusHistory(reportId: String): List<StatusChange> {
        return statusHistory.filter { it.reportId == reportId }
    }
    fun saveReport(
        residentId: String,
        category: Category,
        description: String,
        photoUri: String,
        location: String
    ): Report{
        if (forceSaveFailure){
            throw Exception("Unable to save report. Please try again.")
        }
        val reportId = "NRB-${nextReportNumber.toString().padStart(4, '0')}"
        val report = Report(
            id = reportId,
            residentId = residentId,
            categoryId = category.id,
            description = description,
            photoUri = photoUri,
            location = location,
            dateSubmitted = Date(),
            status = ReportStatus.SUBMITTED,
            priority = category.defaultPriority
        )
        // Save the report
        reports.add(report)

// Record its initial status
        statusHistory.add(
            StatusChange(
                reportId = reportId,
                status = ReportStatus.SUBMITTED,
                changedBy = residentId,
                dateChanged = Date()
            )
        )

        nextReportNumber++

        return report
    }
}