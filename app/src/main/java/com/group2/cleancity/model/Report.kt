package com.group2.cleancity.model

/**
 * Data class representing an urban waste or sanitation report submitted by a resident.
 * Stores all essential report details, current status, priority, officer assignment, and internal notes.
 */
data class Report(
    val id: String, // Unique generated identifier string (e.g., "NRB-0001")
    val reporterId: Int, // ID of resident user who submitted the report
    val reporterName: String, // Full name of resident reporter
    val reporterContact: String, // Phone number or email of resident (officer view only)
    val categoryName: String, // Category name (e.g., "Illegal Dumping", "Blocked Drainage")
    val description: String, // Textual description of problem provided by resident
    val photoUri: String?, // String path/URI of optional attached photo evidence
    val locationText: String, // Typed location address or landmark (e.g., "Argwings Kodhek Rd, Kilimani")
    var status: ReportStatus = ReportStatus.SUBMITTED, // Current status (defaults to SUBMITTED)
    var priority: Priority = Priority.MEDIUM, // Urgency priority level
    var assignedOfficerId: Int? = null, // ID of officer assigned to handle report
    var assignedOfficerName: String? = null, // Full name of assigned officer
    var resolutionNotes: String? = null, // Required details recorded upon resolving the report
    val internalNotes: MutableList<String> = mutableListOf(), // Staff-only internal timestamped notes
    val createdAt: Long = System.currentTimeMillis(), // Submission timestamp in epoch milliseconds
    var updatedAt: Long = System.currentTimeMillis() // Last modification timestamp
)