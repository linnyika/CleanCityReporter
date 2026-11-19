package com.group2.cleancity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.SettingsRepository
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentAdminHomeBinding
import com.group2.cleancity.model.Role

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        binding.tvAdminName.text =
            "Welcome, ${SessionManager.currentUser?.fullName ?: "Administrator"}"

        displayDashboardStats()

        binding.btnManageUsers.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_userManagement
            )
        }

        binding.btnManageCategories.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_categoryManagement
            )
        }

        binding.btnManageReports.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_reportManagement
            )
        }

        binding.btnAdminAnalytics.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_adminAnalytics
            )
        }

        binding.btnAuditHistory.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_auditHistory
            )
        }

        binding.btnSystemSettings.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminHome_to_systemSettings
            )
        }

        binding.btnAdminLogout.setOnClickListener {
            SessionManager.logout()

            findNavController().navigate(
                R.id.action_global_to_welcome
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null && SessionManager.hasRole(Role.ADMIN)) {
            displayDashboardStats()
        }
    }

    private fun displayDashboardStats() {
        binding.tvTotalUsers.text =
            UserRepository.getAllUsers().size.toString()

        binding.tvTotalReports.text =
            ReportRepository.getAllReports().size.toString()

        binding.tvOpenReports.text =
            ReportRepository.getUnresolvedCount().toString()

        binding.tvOverdueReports.text =
            ReportRepository.getOverdueCount(
                SettingsRepository.getOverdueThreshold()
            ).toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
