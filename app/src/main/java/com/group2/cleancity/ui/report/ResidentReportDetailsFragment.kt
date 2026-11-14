package com.group2.cleancity.ui.report

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentResidentReportDetailsBinding
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role
import java.text.DateFormat
import java.util.Date

/** Read-only report details for the resident who submitted the report. */
class ResidentReportDetailsFragment : Fragment(R.layout.fragment_resident_report_details) {

    private var _binding: FragmentResidentReportDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResidentReportDetailsBinding.bind(view)

        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) {
            showMessageAndReturn("Please sign in as a resident to view this report.")
            return
        }

        val reportId = arguments?.getString("reportId")
        if (reportId.isNullOrBlank()) {
            showMessageAndReturn("Report ID is missing.")
            return
        }

        val report = ReportRepository.getReportById(reportId)
        if (report == null || report.reporterId != resident.id) {
            showMessageAndReturn("This report could not be found.")
            return
        }

        showReport(report)
        showTimeline(report)
    }

    private fun showReport(report: Report) {
        binding.tvResidentDetailId.text = report.id
        binding.tvResidentDetailStatus.text = report.status.name.replace('_', ' ')
        binding.tvResidentDetailCategory.text = report.categoryName
        binding.tvResidentDetailPriority.text = "Priority: ${report.priority.name.lowercase().replaceFirstChar { it.uppercase() }}"
        binding.tvResidentDetailDescription.text = report.description
        binding.tvResidentDetailLocation.text = report.locationText
        binding.tvResidentDetailDate.text = formatDate(report.createdAt)

        // Reset the image first so a missing photo never leaves an old image visible.
        binding.imageResidentReport.setImageResource(android.R.drawable.ic_menu_gallery)
        report.photoUri?.let { photoUri ->
            try {
                binding.imageResidentReport.setImageURI(Uri.parse(photoUri))
            } catch (_: Exception) {
                binding.imageResidentReport.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        val resolutionDetails = report.resolutionNotes
        if (report.status == ReportStatus.RESOLVED && !resolutionDetails.isNullOrBlank()) {
            binding.tvResolutionDetails.text = resolutionDetails
            binding.cardResolutionDetails.visibility = View.VISIBLE
        } else {
            binding.cardResolutionDetails.visibility = View.GONE
        }
    }

    private fun showTimeline(report: Report) {
        binding.timelineContainer.removeAllViews()

        val statusOrder = listOf(
            ReportStatus.SUBMITTED,
            ReportStatus.ACKNOWLEDGED,
            ReportStatus.IN_PROGRESS,
            ReportStatus.RESOLVED
        )
        val currentIndex = statusOrder.indexOf(report.status)
        val statusHistory = ReportRepository.getStatusChanges(report.id)

        statusOrder.forEachIndexed { index, status ->
            val historyEntry = statusHistory.firstOrNull { it.newStatus == status }
            val isCompleted = index <= currentIndex
            val isCurrent = status == report.status

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 10, 12, 10)
                if (isCurrent) setBackgroundColor(0xFFE8F5E9.toInt())
            }

            val title = TextView(requireContext()).apply {
                text = status.name.replace('_', ' ') + if (isCurrent) "  •  CURRENT" else ""
                textSize = 15f
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isCompleted) R.color.secondary else android.R.color.darker_gray
                    )
                )
                if (isCurrent) setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
            row.addView(title)

            val detail = TextView(requireContext()).apply {
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                if (isCompleted) {
                    val person = historyEntry?.changedByName
                        ?: if (status == ReportStatus.SUBMITTED) report.reporterName else "Staff member"
                    val timestamp = historyEntry?.timestamp
                        ?: if (status == ReportStatus.SUBMITTED) report.createdAt else null
                    text = if (timestamp != null) {
                        "Completed by $person · ${formatDate(timestamp)}"
                    } else {
                        "Completed"
                    }
                } else {
                    text = "Not reached yet"
                }
            }
            row.addView(detail)

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (8 * resources.displayMetrics.density).toInt()
            }
            binding.timelineContainer.addView(row, rowParams)
        }
    }

    private fun formatDate(timestamp: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(timestamp))
    }

    private fun showMessageAndReturn(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}