package com.group2.cleancity.model

import java.util.Date

data class StatusChange(
    val reportId: String,
    val status: ReportStatus,
    val changedBy: String,
    val dateChanged: Date
)