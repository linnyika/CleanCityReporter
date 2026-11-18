package com.group2.cleancity.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.SettingsRepository
import com.group2.cleancity.databinding.FragmentAdminAnalyticsBinding
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role

class AdminAnalyticsFragment : Fragment() {

    private var _binding: FragmentAdminAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAnalyticsBinding.inflate(
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

        displayAnalytics()
    }

    private fun displayAnalytics() {
        if (_binding == null) return

        val reports = ReportRepository.getAllReports()

        binding.tvAnalyticsTotalReports.text = reports.size.toString()

        binding.tvAnalyticsOverdueReports.text =
            ReportRepository.getOverdueCount(
                SettingsRepository.getOverdueThreshold()
            ).toString()

        // Reports by status
        binding.statusAnalyticsContainer.removeAllViews()

        ReportStatus.values().forEach { status ->
            val count = reports.count { it.status == status }

            addAnalyticsRow(
                binding.statusAnalyticsContainer,
                status.name.replace('_', ' '),
                count
            )
        }

        // Reports by priority
        binding.priorityAnalyticsContainer.removeAllViews()

        Priority.values().forEach { priority ->
            val count = reports.count { it.priority == priority }

            addAnalyticsRow(
                binding.priorityAnalyticsContainer,
                priority.name,
                count
            )
        }

        // Reports by category
        binding.categoryAnalyticsContainer.removeAllViews()

        reports.groupingBy { it.categoryName }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .forEach { (category, count) ->
                addAnalyticsRow(
                    binding.categoryAnalyticsContainer,
                    category,
                    count
                )
            }

        if (reports.isEmpty()) {
            binding.categoryAnalyticsContainer.addView(
                TextView(requireContext()).apply {
                    text = "No report data available yet."
                    textSize = 14f
                }
            )
        }
    }

    private fun addAnalyticsRow(
        container: ViewGroup,
        label: String,
        count: Int
    ) {
        val row = TextView(requireContext()).apply {
            text = "$label: $count"
            textSize = 15f
            setTextColor(
                resources.getColor(
                    android.R.color.black,
                    requireContext().theme
                )
            )
            setPadding(0, 8, 0, 8)
        }

        container.addView(row)
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null && SessionManager.hasRole(Role.ADMIN)) {
            displayAnalytics()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
