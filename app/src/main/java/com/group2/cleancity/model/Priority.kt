package com.group2.cleancity.model

/**
 * Enum class representing the urgency priority level of a sanitation report.
 */
enum class Priority {
    LOW,    // Low urgency (routine maintenance)
    MEDIUM, // Standard urgency
    HIGH,   // High urgency (causes significant blockage or health hazard)
    URGENT  // Immediate response required (e.g. severe flooding or burst sewer)
}
