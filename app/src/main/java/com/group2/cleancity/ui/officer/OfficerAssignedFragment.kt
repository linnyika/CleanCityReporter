package com.group2.cleancity.ui.officer

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
import com.group2.cleancity.databinding.FragmentOfficerAssignedBinding
import com.group2.cleancity.model.Role

class OfficerAssignedFragment : Fragment() {

    private var _binding: FragmentOfficerAssignedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OfficerReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficerAssignedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.OFFICER)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        setupRecyclerView()
        refreshAssignedReports()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.isLoggedIn) {
            refreshAssignedReports()
        }
    }

    private fun setupRecyclerView() {
        adapter = OfficerReportAdapter(emptyList()) { selectedReport ->
            val bundle = Bundle().apply { putString("reportId", selectedReport.id) }
            findNavController().navigate(R.id.officerReportDetailsFragment, bundle)
        }
        binding.rvAssignedReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignedReports.adapter = adapter
    }

    private fun refreshAssignedReports() {
        val officerId = SessionManager.currentUser?.id ?: return
        val assignedReports = ReportRepository.getAllReports()
            .filter { it.assignedOfficerId == officerId }
            .sortedByDescending { it.createdAt }

        adapter.updateReports(assignedReports)

        if (assignedReports.isEmpty()) {
            binding.tvAssignedEmpty.visibility = View.VISIBLE
            binding.rvAssignedReports.visibility = View.GONE
        } else {
            binding.tvAssignedEmpty.visibility = View.GONE
            binding.rvAssignedReports.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        binding.rvAssignedReports.adapter = null
        super.onDestroyView()
        _binding = null
    }
}