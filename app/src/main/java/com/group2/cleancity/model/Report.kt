package com.group2.cleancity.model

import java.util.Date
data class Report(
    val id: String,
    val residentId: String,
    val categoryId: String,
    val description: String,
    val photoUri: String,
    val location: String,
    val dateSubmitted: Date,
    val status: ReportStatus,
    val priority: Priority
)