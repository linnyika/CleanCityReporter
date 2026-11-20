package com.group2.cleancity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentResidentHomeBinding
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role
import com.group2.cleancity.ui.report.MyReportAdapter

class ResidentHomeFragment : Fragment() {

    private var _binding: FragmentResidentHomeBinding? = null
    private val binding get() = _binding!!

    private var reportAdapter: MyReportAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: only a signed-in Resident may be here
        if (!SessionManager.hasRole(Role.RESIDENT)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        setupRecyclerView()
        showGreeting()
        updateDashboard()

        // Slice 3: open the report form (green card and the + button)
        binding.cardReport.setOnClickListener {
            findNavController().navigate(R.id.action_residentHome_to_report)
        }
        binding.fabReport.setOnClickListener {
            findNavController().navigate(R.id.action_residentHome_to_report)
        }

        // Navigate to My Reports screen (same as bottom nav tab)
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }
    }

    private fun setupRecyclerView() {
        reportAdapter = MyReportAdapter { reportId ->
            val arguments = Bundle().apply { putString("reportId", reportId) }
            findNavController().navigate(R.id.residentReportDetailsFragment, arguments)
        }
        binding.recyclerRecentReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentReports.adapter = reportAdapter
    }

    // Refresh the numbers and recent reports when returning to this screen
    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.isLoggedIn) {
            updateDashboard()
        }
    }

    private fun updateDashboard() {
        showCounts()
        showRecentReports()
    }

    private fun showGreeting() {
        val fullName = SessionManager.currentUser?.fullName ?: ""
        // "Demo Resident" -> "Demo"
        val firstName = fullName.trim().substringBefore(" ")
        binding.tvGreeting.text = "Hello, $firstName"
    }

    private fun showCounts() {
        val user = SessionManager.currentUser ?: return
        val myReports = ReportRepository.getReportsByReporter(user.id)

        binding.tvCountTotal.text = myReports.size.toString()
        binding.tvCountInProgress.text =
            myReports.count { it.status == ReportStatus.IN_PROGRESS }.toString()
        binding.tvCountResolved.text =
            myReports.count { it.status == ReportStatus.RESOLVED }.toString()
    }

    private fun showRecentReports() {
        val user = SessionManager.currentUser ?: return
        val adapter = reportAdapter ?: return

        val recentReports = ReportRepository.getReportsByReporter(user.id)
            .sortedByDescending { it.createdAt }
            .take(3)

        adapter.submitReports(recentReports)

        val hasReports = recentReports.isNotEmpty()
        binding.recyclerRecentReports.visibility = if (hasReports) View.VISIBLE else View.GONE
        binding.tvRecentEmpty.visibility = if (hasReports) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        binding.recyclerRecentReports.adapter = null
        reportAdapter = null
        super.onDestroyView()
        _binding = null
    }
}