
package com.group2.cleancity.ui.admin

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.SettingsRepository
import com.group2.cleancity.databinding.FragmentReportManagementBinding
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class ReportManagementFragment : Fragment() {

    private var _binding: FragmentReportManagementBinding? = null
    private val binding get() = _binding!!

    private var selectedStatus: ReportStatus? = null
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportManagementBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            Toast.makeText(
                requireContext(),
                "Access denied. Admins only.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        setupFilters()
        displayReports()
    }

    private fun setupFilters() {
        val statuses = listOf("All statuses") +
                ReportStatus.values().map {
                    it.name.replace('_', ' ')
                }

        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        )
        statusAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spinnerReportStatus.adapter = statusAdapter

        binding.spinnerReportStatus.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedStatus = if (position == 0) {
                        null
                    } else {
                        ReportStatus.values()[position - 1]
                    }
                    displayReports()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) = Unit
            }

        setupCategoryFilter()
    }

    private fun setupCategoryFilter() {
        val categories = ReportRepository.getAllReports()
            .map { it.categoryName }
            .distinct()
            .sorted()

        val categoryOptions = listOf("All categories") + categories

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryOptions
        )
        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spinnerReportCategory.adapter = categoryAdapter

        binding.spinnerReportCategory.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = if (position == 0) {
                        null
                    } else {
                        categoryOptions[position]
                    }
                    displayReports()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) = Unit
            }
    }

    private fun displayReports() {
        if (_binding == null) return

        val allReports = ReportRepository.getAllReports()

        val filteredReports = allReports
            .filter { report ->
                selectedStatus == null || report.status == selectedStatus
            }
            .filter { report ->
                selectedCategory == null ||
                        report.categoryName == selectedCategory
            }
            .sortedByDescending { it.createdAt }

        binding.reportListContainer.removeAllViews()

        binding.tvReportCount.text =
            "Showing ${filteredReports.size} of ${allReports.size} reports"

        binding.tvNoReports.visibility =
            if (filteredReports.isEmpty()) View.VISIBLE else View.GONE

        filteredReports.forEach { report ->
            binding.reportListContainer.addView(createReportCard(report))
        }
    }

    private fun createReportCard(report: Report): View {
        val context = requireContext()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            isClickable = true
            isFocusable = true
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 20)
        }
        card.layoutParams = params

        val title = TextView(context).apply {
            text = report.id
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
        }

        val category = TextView(context).apply {
            text = report.categoryName
            textSize = 15f
        }

        val status = TextView(context).apply {
            text = "Status: ${report.status.name.replace('_', ' ')}"
            textSize = 14f
        }

        val priority = TextView(context).apply {
            text = "Priority: ${report.priority.name}"
            textSize = 14f
        }

        card.addView(title)
        card.addView(category)
        card.addView(status)
        card.addView(priority)

        if (isOverdue(report)) {
            val overdueLabel = TextView(context).apply {
                text = "OVERDUE"
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_red_dark
                    )
                )
                setPadding(0, 8, 0, 0)
            }
            card.addView(overdueLabel)
        }

        card.setOnClickListener {
            showReportDetails(report)
        }

        return card
    }

    private fun isOverdue(report: Report): Boolean {
        if (report.status == ReportStatus.RESOLVED) return false

        val thresholdDays = SettingsRepository.getOverdueThreshold()
        val age = System.currentTimeMillis() - report.createdAt
        val threshold = TimeUnit.DAYS.toMillis(thresholdDays.toLong())

        return age > threshold
    }

    private fun showReportDetails(report: Report) {
        val assignedOfficer = report.assignedOfficerName ?: "Not assigned"
        val resolutionNotes = report.resolutionNotes ?: "None"

        val details = """
            Report ID: ${report.id}
            Reporter: ${report.reporterName}
            Contact: ${report.reporterContact}
            Category: ${report.categoryName}
            Status: ${report.status.name.replace('_', ' ')}
            Priority: ${report.priority.name}
            Location: ${report.locationText}
            Assigned Officer: $assignedOfficer

            Description:
            ${report.description}

            Date Submitted:
            ${DateFormat.getDateTimeInstance().format(Date(report.createdAt))}

            Resolution Notes:
            $resolutionNotes
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Report Details")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null &&
            SessionManager.hasRole(Role.ADMIN)
        ) {
            setupCategoryFilter()
            displayReports()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
