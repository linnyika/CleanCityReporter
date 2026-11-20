package com.group2.cleancity.ui.officer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentOfficerDashboardBinding
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role

class OfficerDashboardFragment : Fragment() {

    private var _binding: FragmentOfficerDashboardBinding? = null
    private val binding get() = _binding!!

    private var needsAttentionAdapter: OfficerReportAdapter? = null
    private var assignedAdapter: OfficerReportAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.OFFICER)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        setupRecyclerViews()
        showGreeting()
        updateDashboard()

        binding.cardIncoming.setOnClickListener {
            val bundle = bundleOf("filterStatus" to "SUBMITTED")
            findNavController().navigate(R.id.action_officerHome_to_officerQueue, bundle)
        }

        binding.cardAssigned.setOnClickListener {
            findNavController().navigate(R.id.officerAssignedFragment)
        }

        binding.cardInProgress.setOnClickListener {
            val bundle = bundleOf("filterStatus" to "IN_PROGRESS")
            findNavController().navigate(R.id.action_officerHome_to_officerQueue, bundle)
        }

        binding.cardUnresolved.setOnClickListener {
            val bundle = bundleOf("filterType" to "UNRESOLVED")
            findNavController().navigate(R.id.action_officerHome_to_officerQueue, bundle)
        }

        binding.cardOverdue.setOnClickListener {
            val bundle = bundleOf("filterType" to "OVERDUE")
            findNavController().navigate(R.id.action_officerHome_to_officerQueue, bundle)
        }

        binding.btnViewQueue.setOnClickListener {
            findNavController().navigate(R.id.action_officerHome_to_officerQueue)
        }

        binding.tvNeedsAttentionSeeAll.setOnClickListener {
            findNavController().navigate(R.id.officerReportQueueFragment)
        }

        binding.tvAssignedSeeAll.setOnClickListener {
            findNavController().navigate(R.id.officerAssignedFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.isLoggedIn) {
            updateDashboard()
        }
    }

    private fun setupRecyclerViews() {
        needsAttentionAdapter = OfficerReportAdapter(emptyList()) { report ->
            openReportDetails(report.id)
        }
        binding.rvNeedsAttention.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNeedsAttention.isNestedScrollingEnabled = false
        binding.rvNeedsAttention.adapter = needsAttentionAdapter

        assignedAdapter = OfficerReportAdapter(emptyList()) { report ->
            openReportDetails(report.id)
        }
        binding.rvAssignedToMe.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignedToMe.isNestedScrollingEnabled = false
        binding.rvAssignedToMe.adapter = assignedAdapter
    }

    private fun showGreeting() {
        val fullName = SessionManager.currentUser?.fullName ?: ""
        val firstName = fullName.trim().substringBefore(" ")
        binding.tvGreeting.text = "Hello, $firstName"
    }

    private fun updateDashboard() {
        val user = SessionManager.currentUser ?: return

        binding.tvCountIncoming.text = ReportRepository.getSubmittedCount().toString()
        binding.tvCountAssigned.text =
            ReportRepository.getAssignedToOfficerCount(user.id).toString()
        binding.tvCountInProgress.text = ReportRepository.getInProgressCount().toString()
        binding.tvCountUnresolved.text = ReportRepository.getUnresolvedCount().toString()
        binding.tvCountOverdue.text = ReportRepository.getOverdueCount().toString()

        showNeedsAttention()
        showAssignedToMe(user.id)
    }

    private fun showNeedsAttention() {
        val reports = ReportRepository.getAllReports()
            .filter { it.status == ReportStatus.SUBMITTED }
            .sortedBy { it.createdAt }
            .take(3)

        needsAttentionAdapter?.updateReports(reports)

        val hasReports = reports.isNotEmpty()
        binding.rvNeedsAttention.visibility = if (hasReports) View.VISIBLE else View.GONE
        binding.cardNeedsAttentionEmpty.visibility =
            if (hasReports) View.GONE else View.VISIBLE
    }

    private fun showAssignedToMe(officerId: Int) {
        val reports = ReportRepository.getAllReports()
            .filter { it.assignedOfficerId == officerId }
            .sortedByDescending { it.createdAt }
            .take(3)

        assignedAdapter?.updateReports(reports)

        val hasReports = reports.isNotEmpty()
        binding.rvAssignedToMe.visibility = if (hasReports) View.VISIBLE else View.GONE
        binding.cardAssignedEmpty.visibility =
            if (hasReports) View.GONE else View.VISIBLE
    }

    private fun openReportDetails(reportId: String) {
        val bundle = bundleOf("reportId" to reportId)
        findNavController().navigate(
            R.id.action_officerQueue_to_officerDetails,
            bundle
        )
    }

    override fun onDestroyView() {
        binding.rvNeedsAttention.adapter = null
        binding.rvAssignedToMe.adapter = null
        needsAttentionAdapter = null
        assignedAdapter = null
        super.onDestroyView()
        _binding = null
    }
}
