package com.group2.cleancity.ui.report

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentMyReportsBinding
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role

/** Shows the signed-in resident's own report history. */
class MyReportsFragment : Fragment(R.layout.fragment_my_reports) {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!

    private var reportAdapter: MyReportAdapter? = null
    private var residentId: Int? = null
    private var searchQuery = ""
    private var selectedFilter = ReportFilter.ALL

    private enum class ReportFilter {
        ALL, SUBMITTED, IN_PROGRESS, RESOLVED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyReportsBinding.bind(view)

        // The stored account supplies both the role and the resident ID.
        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }
        residentId = resident.id

        reportAdapter = MyReportAdapter { reportId ->
            // This destination and argument must be added to the shared nav_graph.xml.
            val arguments = Bundle().apply { putString("reportId", reportId) }
            findNavController().navigate(R.id.residentReportDetailsFragment, arguments)
        }

        binding.recyclerMyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyReports.adapter = reportAdapter

        binding.buttonSubmitFirstReport.setOnClickListener {
            findNavController().navigate(R.id.reportFragment)
        }

        binding.etSearchReports.doAfterTextChanged { text ->
            searchQuery = text?.toString()?.trim().orEmpty()
            refreshReports()
        }

        binding.filterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            selectedFilter = when (checkedId) {
                R.id.buttonFilterSubmitted -> ReportFilter.SUBMITTED
                R.id.buttonFilterInProgress -> ReportFilter.IN_PROGRESS
                R.id.buttonFilterResolved -> ReportFilter.RESOLVED
                else -> ReportFilter.ALL
            }
            refreshReports()
        }

        refreshReports()
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case the resident submitted a report and returned here.
        if (residentId != null) refreshReports()
    }

    private fun refreshReports() {
        val currentResidentId = residentId ?: return
        val adapter = reportAdapter ?: return

        val residentReports = ReportRepository
            .getReportsByReporter(currentResidentId)
            .sortedByDescending { it.createdAt }

        val visibleReports = residentReports
            .filter { report -> matchesSelectedFilter(report) }
            .filter { report -> matchesSearch(report, searchQuery) }

        adapter.submitReports(visibleReports)

        val hasReports = visibleReports.isNotEmpty()
        binding.recyclerMyReports.visibility = if (hasReports) View.VISIBLE else View.GONE
        binding.layoutEmptyReports.visibility = if (hasReports) View.GONE else View.VISIBLE
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

    override fun onDestroyView() {
        binding.recyclerMyReports.adapter = null
        reportAdapter = null
        _binding = null
        super.onDestroyView()
    }
}