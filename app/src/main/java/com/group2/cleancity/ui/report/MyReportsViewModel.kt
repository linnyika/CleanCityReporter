package com.group2.cleancity.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus

class MyReportsViewModel : ViewModel() {

    enum class ReportFilter {
        ALL, SUBMITTED, IN_PROGRESS, RESOLVED
    }

    private val _visibleReports = MutableLiveData<List<Report>>(emptyList())
    val visibleReports: LiveData<List<Report>> = _visibleReports

    private var searchQuery = ""
    private var selectedFilter = ReportFilter.ALL

    fun loadReports(residentId: Int) {
        val residentReports = ReportRepository
            .getReportsByReporter(residentId)
            .sortedByDescending { it.createdAt }

        val filtered = residentReports
            .filter { report -> matchesSelectedFilter(report) }
            .filter { report -> matchesSearch(report, searchQuery) }

        _visibleReports.value = filtered
    }

    fun setSearchQuery(query: String, residentId: Int) {
        searchQuery = query.trim()
        loadReports(residentId)
    }

    fun setFilter(filter: ReportFilter, residentId: Int) {
        selectedFilter = filter
        loadReports(residentId)
    }

    private fun matchesSelectedFilter(report: Report): Boolean {
        return when (selectedFilter) {
            ReportFilter.ALL -> true
            ReportFilter.SUBMITTED -> report.status == ReportStatus.SUBMITTED
            ReportFilter.IN_PROGRESS -> report.status == ReportStatus.ACKNOWLEDGED ||
                    report.status == ReportStatus.IN_PROGRESS
            ReportFilter.RESOLVED -> report.status == ReportStatus.RESOLVED
        }
    }

    private fun matchesSearch(report: Report, query: String): Boolean {
        if (query.isBlank()) return true

        return report.id.contains(query, ignoreCase = true) ||
                report.locationText.contains(query, ignoreCase = true) ||
                report.categoryName.contains(query, ignoreCase = true) ||
                report.description.contains(query, ignoreCase = true)
    }
}
