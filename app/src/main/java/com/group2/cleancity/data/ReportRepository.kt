package com.group2.cleancity.data

import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.StatusChange
import com.group2.cleancity.model.User
import java.util.concurrent.TimeUnit

/**
 * In-memory singleton repository for managing sanitation reports,
 * enforcing status state machines, and logging audit/status history.
 */
object ReportRepository {

    // Internal mutable list holding active reports in memory
    private val reports = mutableListOf<Report>()
    // Internal mutable list holding status change audit trails
    private val statusChanges = mutableListOf<StatusChange>()
    private var nextStatusChangeId = 1

    // ADDED (slice 3): lets the group demo the "submission failed" rule
    var forceSaveFailure = false

    init {
        // Seed sample reports for testing Officer Queue and Dashboard immediately
        seedSampleReports()
    }

    // Seeds initial mock reports representing various statuses, locations, and priorities
    private fun seedSampleReports() {
        val now = System.currentTimeMillis()
        val dayInMillis = TimeUnit.DAYS.toMillis(1)

        val r1 = Report(
            id = "NRB-0001",
            reporterId = 4,
            reporterName = "Demo Resident",
            reporterContact = "0712345678",
            categoryName = "Illegal Dumping",
            description = "Large heap of domestic garbage dumped near the shopping center.",
            photoUri = null,
            locationText = "Argwings Kodhek Rd, Kilimani",
            status = ReportStatus.SUBMITTED,
            priority = Priority.HIGH,
            createdAt = now - (2 * dayInMillis),
            updatedAt = now - (2 * dayInMillis)
        )

        val r2 = Report(
            id = "NRB-0002",
            reporterId = 4,
            reporterName = "Demo Resident",
            reporterContact = "0712345678",
            categoryName = "Blocked Drainage",
            description = "Storm drain completely blocked with plastic waste causing minor flooding.",
            photoUri = null,
            locationText = "Ngong Road, near Junction Mall",
            status = ReportStatus.ACKNOWLEDGED,
            priority = Priority.URGENT,
            assignedOfficerId = 2,
            assignedOfficerName = "Achieng Odhiambo",
            createdAt = now - (8 * dayInMillis), // Overdue report (>7 days)
            updatedAt = now - (7 * dayInMillis)
        )

        val r3 = Report(
            id = "NRB-0003",
            reporterId = 4,
            reporterName = "Jane Doe",
            reporterContact = "0722000111",
            categoryName = "Uncollected Waste",
            description = "Household rubbish bins have not been emptied for 2 weeks.",
            photoUri = null,
            locationText = "Waiyaki Way, Westlands",
            status = ReportStatus.IN_PROGRESS,
            priority = Priority.MEDIUM,
            assignedOfficerId = 3,
            assignedOfficerName = "Brian Otieno",
            createdAt = now - (3 * dayInMillis),
            updatedAt = now - (1 * dayInMillis)
        )
        r3.internalNotes.add("Response team dispatched to Westlands location.")

        val r4 = Report(
            id = "NRB-0004",
            reporterId = 4,
            reporterName = "John Maina",
            reporterContact = "0733999888",
            categoryName = "Burst Sewer Line",
            description = "Sewer pipe leaking onto the main road near the bus stop.",
            photoUri = null,
            locationText = "Tom Mboya Street, CBD",
            status = ReportStatus.RESOLVED,
            priority = Priority.URGENT,
            assignedOfficerId = 2,
            assignedOfficerName = "Achieng Odhiambo",
            resolutionNotes = "Sewer pipe repaired and sanitized by City Council maintenance team.",
            createdAt = now - (5 * dayInMillis),
            updatedAt = now - (1 * dayInMillis)
        )

        reports.addAll(listOf(r1, r2, r3, r4))

        // Record initial status change history for seeded reports
        recordStatusChange(r1.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
        recordStatusChange(r2.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
        recordStatusChange(r2.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged by officer")
        recordStatusChange(r3.id, null, ReportStatus.SUBMITTED, 4, "Jane Doe", "Initial submission")
        recordStatusChange(r3.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 3, "Brian Otieno", "Acknowledged by officer")
        recordStatusChange(r3.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 3, "Brian Otieno", "Team assigned to clean site")
        recordStatusChange(r4.id, null, ReportStatus.SUBMITTED, 4, "John Maina", "Initial submission")
        recordStatusChange(r4.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged")
        recordStatusChange(r4.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 2, "Achieng Odhiambo", "Fix in progress")
        recordStatusChange(r4.id, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, 2, "Achieng Odhiambo", "Pipe repaired")
    }

    // Returns copy of all reports currently stored
    fun getAllReports(): List<Report> = reports.toList()

    // Finds a report by its unique String ID (e.g., "NRB-0001")
    fun getReportById(id: String): Report? = reports.find { it.id.equals(id, ignoreCase = true) }

    // ADDED (slice 4 will need this): only one resident's own reports
    fun getReportsByReporter(reporterId: Int): List<Report> =
        reports.filter { it.reporterId == reporterId }

    /**
     * ADDED (slice 3): creates and saves a new report for a resident.
     * Gets the next ID after the seeded ones (NRB-0005, NRB-0006, ...),
     * starts the report as SUBMITTED, and records the first status change.
     * Throws an exception if saving fails, so the screen never shows false success.
     */
    fun saveReport(
        reporter: User,
        category: Category,
        description: String,
        photoUri: String,
        locationText: String
    ): Report {
        if (forceSaveFailure) {
            throw Exception("Unable to save report. Please try again.")
        }

        val now = System.currentTimeMillis()
        val reportId = "NRB-${(reports.size + 1).toString().padStart(4, '0')}"

        val report = Report(
            id = reportId,
            reporterId = reporter.id,
            reporterName = reporter.fullName,
            reporterContact = reporter.phone,
            categoryName = category.name,
            description = description.trim(),
            photoUri = photoUri,
            locationText = locationText.trim(),
            status = ReportStatus.SUBMITTED,
            priority = category.defaultPriority,
            createdAt = now,
            updatedAt = now
        )

        reports.add(report)
        recordStatusChange(
            reportId, null, ReportStatus.SUBMITTED,
            reporter.id, reporter.fullName, "Initial submission"
        )
        return report
    }

    // DASHBOARD METRIC HELPERS:

    // Returns total count of newly SUBMITTED reports awaiting review
    fun getSubmittedCount(): Int = reports.count { it.status == ReportStatus.SUBMITTED }

    // Returns total count of active (unresolved) reports assigned to a specific officer ID
    fun getAssignedToOfficerCount(officerId: Int): Int =
        reports.count { it.assignedOfficerId == officerId && it.status != ReportStatus.RESOLVED }

    // Returns total count of reports currently IN_PROGRESS
    fun getInProgressCount(): Int = reports.count { it.status == ReportStatus.IN_PROGRESS }

    // Returns total count of all reports not yet RESOLVED
    fun getUnresolvedCount(): Int = reports.count { it.status != ReportStatus.RESOLVED }

    // Returns count of unresolved reports created over thresholdDays ago (default 7 days)
    fun getOverdueCount(thresholdDays: Int = 7): Int {
        val thresholdMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(thresholdDays.toLong())
        return reports.count { it.status != ReportStatus.RESOLVED && it.createdAt < thresholdMillis }
    }

    /**
     * STATE MACHINE GUARD:
     * Enforces the strict sequential status transition rule:
     * SUBMITTED -> ACKNOWLEDGED -> IN_PROGRESS -> RESOLVED.
     */
    fun isValidTransition(current: ReportStatus, next: ReportStatus): Boolean {
        return when (current) {
            ReportStatus.SUBMITTED -> next == ReportStatus.ACKNOWLEDGED
            ReportStatus.ACKNOWLEDGED -> next == ReportStatus.IN_PROGRESS
            ReportStatus.IN_PROGRESS -> next == ReportStatus.RESOLVED
            ReportStatus.RESOLVED -> false // Terminal state: cannot transition after resolution
        }
    }

    // Transitions status from SUBMITTED to ACKNOWLEDGED
    fun acknowledgeReport(reportId: String, officer: User): Boolean {
        val report = getReportById(reportId) ?: return false
        if (report.status != ReportStatus.SUBMITTED) return false

        val oldStatus = report.status
        report.status = ReportStatus.ACKNOWLEDGED
        if (report.assignedOfficerId == null) {
            report.assignedOfficerId = officer.id
            report.assignedOfficerName = officer.fullName
        }
        report.updatedAt = System.currentTimeMillis()

        // Log status change history & system audit
        recordStatusChange(reportId, oldStatus, ReportStatus.ACKNOWLEDGED, officer.id, officer.fullName, "Acknowledged report")
        AuditRepository.log("ACKNOWLEDGE_REPORT", "Report $reportId acknowledged", officer.id, officer.fullName)
        return true
    }

    // Assigns an officer to a report
    fun assignOfficer(reportId: String, officerId: Int, officerName: String, assignedBy: User): Boolean {
        val report = getReportById(reportId) ?: return false
        report.assignedOfficerId = officerId
        report.assignedOfficerName = officerName
        report.updatedAt = System.currentTimeMillis()

        // Log system audit
        AuditRepository.log("ASSIGN_OFFICER", "Report $reportId assigned to $officerName", assignedBy.id, assignedBy.fullName)
        return true
    }

    /**
     * Updates status with workflow sequence validation and mandatory resolution notes requirement.
     * Returns null on success, or an error message string if validation fails.
     */
    fun updateStatus(reportId: String, nextStatus: ReportStatus, officer: User, resolutionNotes: String?): String? {
        val report = getReportById(reportId) ?: return "Report not found"

        // Step 1: Enforce valid state transition
        if (!isValidTransition(report.status, nextStatus)) {
            return "Invalid status transition from ${report.status} to $nextStatus. Transitions must follow SUBMITTED -> ACKNOWLEDGED -> IN_PROGRESS -> RESOLVED."
        }

        // Step 2: Enforce mandatory resolution details when marking as RESOLVED
        if (nextStatus == ReportStatus.RESOLVED) {
            if (resolutionNotes.isNullOrBlank()) {
                return "Resolution details are required to mark a report as RESOLVED."
            }
            report.resolutionNotes = resolutionNotes.trim()
        }

        val oldStatus = report.status
        report.status = nextStatus
        report.updatedAt = System.currentTimeMillis()

        // Step 3: Record status change history & audit entry
        recordStatusChange(reportId, oldStatus, nextStatus, officer.id, officer.fullName, resolutionNotes)
        AuditRepository.log("STATUS_UPDATE", "Report $reportId status changed from $oldStatus to $nextStatus", officer.id, officer.fullName)
        return null
    }

    // Updates priority level of a report
    fun updatePriority(reportId: String, newPriority: Priority, officer: User): Boolean {
        val report = getReportById(reportId) ?: return false
        val oldPriority = report.priority
        report.priority = newPriority
        report.updatedAt = System.currentTimeMillis()

        AuditRepository.log("PRIORITY_UPDATE", "Report $reportId priority changed from $oldPriority to $newPriority", officer.id, officer.fullName)
        return true
    }

    // Adds a timestamped internal staff note to a report
    fun addInternalNote(reportId: String, note: String, officer: User): Boolean {
        if (note.isBlank()) return false
        val report = getReportById(reportId) ?: return false
        val formattedNote = "[${officer.fullName}]: ${note.trim()}"
        report.internalNotes.add(formattedNote)
        report.updatedAt = System.currentTimeMillis()

        AuditRepository.log("ADD_INTERNAL_NOTE", "Added internal note to report $reportId", officer.id, officer.fullName)
        return true
    }

    // Returns list of all status changes recorded for a specific report ID
    fun getStatusChanges(reportId: String): List<StatusChange> =
        statusChanges.filter { it.reportId.equals(reportId, ignoreCase = true) }.sortedBy { it.timestamp }

    // Helper method to record a new StatusChange object
    private fun recordStatusChange(reportId: String, oldStatus: ReportStatus?, newStatus: ReportStatus, userId: Int, userName: String, note: String?) {
        statusChanges.add(
            StatusChange(
                id = nextStatusChangeId++,
                reportId = reportId,
                oldStatus = oldStatus,
                newStatus = newStatus,
                changedByUserId = userId,
                changedByName = userName,
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
    }
}