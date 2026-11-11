package com.group2.cleancity.model

/**
 * Enum class representing the official lifecycle stages of a sanitation report.
 * The enforced sequential progression rule is: SUBMITTED -> ACKNOWLEDGED -> IN_PROGRESS -> RESOLVED.
 */
enum class ReportStatus {
    SUBMITTED,   // Initial stage when a resident submits a report
    ACKNOWLEDGED,// Officer has reviewed and acknowledged the incoming report
    IN_PROGRESS, // Response team has been dispatched and action is underway
    RESOLVED     // Work is completed and resolution details have been recorded
}
