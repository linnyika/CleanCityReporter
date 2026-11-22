package com.group2.cleancity.data

import android.content.Context
import com.group2.cleancity.data.db.AppDatabase
import com.group2.cleancity.data.db.ReportDao
import com.group2.cleancity.data.db.ReportEntity
import com.group2.cleancity.data.db.StatusChangeDao
import com.group2.cleancity.data.db.StatusChangeEntity
import com.group2.cleancity.model.Category
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.StatusChange
import com.group2.cleancity.model.User
import java.util.concurrent.TimeUnit

/**
 * In-memory / Room singleton repository for managing sanitation reports,
 * enforcing status state machines, and logging audit/status history.
 */
object ReportRepository {

    private var reportDao: ReportDao? = null
    private var statusChangeDao: StatusChangeDao? = null

    // Fallback lists for tests without context
    private val fallbackReports = mutableListOf<Report>()
    private val fallbackStatusChanges = mutableListOf<StatusChange>()
    private var fallbackNextStatusChangeId = 1

    // ADDED (slice 3): lets the group demo the "submission failed" rule
    var forceSaveFailure = false

    init {
        if (fallbackReports.isEmpty()) {
            seedSampleReportsFallback()
        }
    }

    fun init(context: Context) {
        if (reportDao == null) {
            val db = AppDatabase.getInstance(context)
            reportDao = db.reportDao()
            statusChangeDao = db.statusChangeDao()
            seedIfNeeded()
        }
    }

    // Seeds initial mock reports representing various statuses, locations, and priorities (fallback)
    private fun seedSampleReportsFallback() {
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

        fallbackReports.addAll(listOf(r1, r2, r3, r4))

        recordStatusChangeFallback(r1.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
        recordStatusChangeFallback(r2.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
        recordStatusChangeFallback(r2.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged by officer")
        recordStatusChangeFallback(r3.id, null, ReportStatus.SUBMITTED, 4, "Jane Doe", "Initial submission")
        recordStatusChangeFallback(r3.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 3, "Brian Otieno", "Acknowledged by officer")
        recordStatusChangeFallback(r3.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 3, "Brian Otieno", "Team assigned to clean site")
        recordStatusChangeFallback(r4.id, null, ReportStatus.SUBMITTED, 4, "John Maina", "Initial submission")
        recordStatusChangeFallback(r4.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged")
        recordStatusChangeFallback(r4.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 2, "Achieng Odhiambo", "Fix in progress")
        recordStatusChangeFallback(r4.id, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, 2, "Achieng Odhiambo", "Pipe repaired")
    }

    private fun seedIfNeeded() {
        val rDao = reportDao ?: return
        val scDao = statusChangeDao ?: return
        if (rDao.getAll().isEmpty()) {
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
                createdAt = now - (8 * dayInMillis),
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

            listOf(r1, r2, r3, r4).forEach { rDao.insert(ReportEntity.toEntity(it)) }

            var scId = 1
            fun recordSeedChange(reportId: String, oldStatus: ReportStatus?, newStatus: ReportStatus, userId: Int, userName: String, note: String?) {
                scDao.insert(
                    StatusChangeEntity(
                        id = scId++,
                        reportId = reportId,
                        oldStatus = oldStatus?.name,
                        newStatus = newStatus.name,
                        changedByUserId = userId,
                        changedByName = userName,
                        timestamp = System.currentTimeMillis(),
                        note = note
                    )
                )
            }

            recordSeedChange(r1.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
            recordSeedChange(r2.id, null, ReportStatus.SUBMITTED, 4, "Demo Resident", "Initial submission")
            recordSeedChange(r2.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged by officer")
            recordSeedChange(r3.id, null, ReportStatus.SUBMITTED, 4, "Jane Doe", "Initial submission")
            recordSeedChange(r3.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 3, "Brian Otieno", "Acknowledged by officer")
            recordSeedChange(r3.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 3, "Brian Otieno", "Team assigned to clean site")
            recordSeedChange(r4.id, null, ReportStatus.SUBMITTED, 4, "John Maina", "Initial submission")
            recordSeedChange(r4.id, ReportStatus.SUBMITTED, ReportStatus.ACKNOWLEDGED, 2, "Achieng Odhiambo", "Acknowledged")
            recordSeedChange(r4.id, ReportStatus.ACKNOWLEDGED, ReportStatus.IN_PROGRESS, 2, "Achieng Odhiambo", "Fix in progress")
            recordSeedChange(r4.id, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, 2, "Achieng Odhiambo", "Pipe repaired")
        }
    }

    private fun recordStatusChangeFallback(reportId: String, oldStatus: ReportStatus?, newStatus: ReportStatus, userId: Int, userName: String, note: String?) {
        fallbackStatusChanges.add(
            StatusChange(
                id = fallbackNextStatusChangeId++,
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

    fun getAllReports(): List<Report> {
        val dao = reportDao
        if (dao != null) {
            return dao.getAll().map { it.toModel() }
        }
        return fallbackReports.toList()
    }

    fun getReportById(id: String): Report? {
        val dao = reportDao
        if (dao != null) {
            return dao.getById(id)?.toModel()
        }
        return fallbackReports.find { it.id.equals(id, ignoreCase = true) }
    }

    fun getReportsByReporter(reporterId: Int): List<Report> {
        val dao = reportDao
        if (dao != null) {
            return dao.getByReporter(reporterId).map { it.toModel() }
        }
        return fallbackReports.filter { it.reporterId == reporterId }
    }

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

        val allReports = getAllReports()
        val now = System.currentTimeMillis()
        val reportId = "NRB-${(allReports.size + 1).toString().padStart(4, '0')}"

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

        val dao = reportDao
        if (dao != null) {
            dao.insert(ReportEntity.toEntity(report))
        } else {
            fallbackReports.add(report)
        }

        recordStatusChange(
            reportId, null, ReportStatus.SUBMITTED,
            reporter.id, reporter.fullName, "Initial submission"
        )
        return report
    }

    fun getSubmittedCount(): Int = getAllReports().count { it.status == ReportStatus.SUBMITTED }

    fun getAssignedToOfficerCount(officerId: Int): Int =
        getAllReports().count { it.assignedOfficerId == officerId && it.status != ReportStatus.RESOLVED }

    fun getInProgressCount(): Int = getAllReports().count { it.status == ReportStatus.IN_PROGRESS }

    fun getUnresolvedCount(): Int = getAllReports().count { it.status != ReportStatus.RESOLVED }

    fun getOverdueCount(thresholdDays: Int = 7): Int {
        val thresholdMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(thresholdDays.toLong())
        return getAllReports().count { it.status != ReportStatus.RESOLVED && it.createdAt < thresholdMillis }
    }

    fun isValidTransition(current: ReportStatus, next: ReportStatus): Boolean {
        return when (current) {
            ReportStatus.SUBMITTED -> next == ReportStatus.ACKNOWLEDGED
            ReportStatus.ACKNOWLEDGED -> next == ReportStatus.IN_PROGRESS
            ReportStatus.IN_PROGRESS -> next == ReportStatus.RESOLVED
            ReportStatus.RESOLVED -> false
        }
    }

    fun acknowledgeReport(reportId: String, officer: User): Boolean {
        val report = getReportById(reportId) ?: return false
        if (report.status != ReportStatus.SUBMITTED) return false

        if (report.assignedOfficerId == null) {
            report.assignedOfficerId = officer.id
            report.assignedOfficerName = officer.fullName
            val dao = reportDao
            if (dao != null) {
                dao.update(ReportEntity.toEntity(report)) // ADDED: persist report update in Room
            }
        }

        val err = updateStatus(reportId, ReportStatus.ACKNOWLEDGED, officer, null)
        return err == null
    }

    fun assignOfficer(reportId: String, officerId: Int, officerName: String, assignedBy: User): Boolean {
        val report = getReportById(reportId) ?: return false
        report.assignedOfficerId = officerId
        report.assignedOfficerName = officerName
        report.updatedAt = System.currentTimeMillis()

        val dao = reportDao
        if (dao != null) {
            dao.update(ReportEntity.toEntity(report)) // ADDED: persist report update in Room
        }

        AuditRepository.log("ASSIGN_OFFICER", "Report $reportId assigned to $officerName", assignedBy.id, assignedBy.fullName)
        NotificationRepository.add(report.reporterId, reportId, "$reportId has been assigned to $officerName")
        return true
    }

    fun updateStatus(reportId: String, nextStatus: ReportStatus, officer: User, resolutionNotes: String?): String? {
        val report = getReportById(reportId) ?: return "Report not found"

        if (!isValidTransition(report.status, nextStatus)) {
            return "Invalid status transition from ${report.status} to $nextStatus. Transitions must follow SUBMITTED -> ACKNOWLEDGED -> IN_PROGRESS -> RESOLVED."
        }

        if (nextStatus == ReportStatus.RESOLVED) {
            if (resolutionNotes.isNullOrBlank()) {
                return "Resolution details are required to mark a report as RESOLVED."
            }
            report.resolutionNotes = resolutionNotes.trim()
        }

        val oldStatus = report.status
        report.status = nextStatus
        report.updatedAt = System.currentTimeMillis()

        val dao = reportDao
        if (dao != null) {
            dao.update(ReportEntity.toEntity(report)) // ADDED: persist report update in Room
        }

        recordStatusChange(reportId, oldStatus, nextStatus, officer.id, officer.fullName, resolutionNotes)
        AuditRepository.log("STATUS_UPDATE", "Report $reportId status changed from $oldStatus to $nextStatus", officer.id, officer.fullName)

        if (nextStatus == ReportStatus.ACKNOWLEDGED) {
            NotificationRepository.add(report.reporterId, reportId, "$reportId has been acknowledged")
        }
        if (nextStatus == ReportStatus.IN_PROGRESS) {
            NotificationRepository.add(report.reporterId, reportId, "$reportId is now In Progress")
        }
        if (nextStatus == ReportStatus.RESOLVED) {
            NotificationRepository.add(report.reporterId, reportId, "$reportId has been resolved")
        }
        return null
    }

    fun updatePriority(reportId: String, newPriority: Priority, officer: User): Boolean {
        val report = getReportById(reportId) ?: return false
        val oldPriority = report.priority
        report.priority = newPriority
        report.updatedAt = System.currentTimeMillis()

        val dao = reportDao
        if (dao != null) {
            dao.update(ReportEntity.toEntity(report)) // ADDED: persist report update in Room
        }

        AuditRepository.log("PRIORITY_UPDATE", "Report $reportId priority changed from $oldPriority to $newPriority", officer.id, officer.fullName)
        return true
    }

    fun addInternalNote(reportId: String, note: String, officer: User): Boolean {
        if (note.isBlank()) return false
        val report = getReportById(reportId) ?: return false
        val formattedNote = "[${officer.fullName}]: ${note.trim()}"
        report.internalNotes.add(formattedNote)
        report.updatedAt = System.currentTimeMillis()

        val dao = reportDao
        if (dao != null) {
            dao.update(ReportEntity.toEntity(report)) // ADDED: persist report update in Room
        }

        AuditRepository.log("ADD_INTERNAL_NOTE", "Added internal note to report $reportId", officer.id, officer.fullName)
        return true
    }

    fun getStatusChanges(reportId: String): List<StatusChange> {
        val dao = statusChangeDao
        if (dao != null) {
            return dao.getByReportId(reportId).map { it.toModel() }.sortedBy { it.timestamp }
        }
        return fallbackStatusChanges.filter { it.reportId.equals(reportId, ignoreCase = true) }.sortedBy { it.timestamp }
    }

    private fun recordStatusChange(reportId: String, oldStatus: ReportStatus?, newStatus: ReportStatus, userId: Int, userName: String, note: String?) {
        val dao = statusChangeDao
        if (dao != null) {
            val allSc = dao.getAll()
            val nextId = (allSc.maxOfOrNull { it.id } ?: 0) + 1
            dao.insert(
                StatusChangeEntity(
                    id = nextId,
                    reportId = reportId,
                    oldStatus = oldStatus?.name,
                    newStatus = newStatus.name,
                    changedByUserId = userId,
                    changedByName = userName,
                    timestamp = System.currentTimeMillis(),
                    note = note
                )
            )
        } else {
            recordStatusChangeFallback(reportId, oldStatus, newStatus, userId, userName, note)
        }
    }
}
