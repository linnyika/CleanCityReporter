package com.group2.cleancity.ui.home

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.AuditRepository
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentAdminHomeBinding
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role
import com.group2.cleancity.ui.officer.OfficerReportAdapter
import com.group2.cleancity.util.NameFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private var overdueAdapter: OfficerReportAdapter? = null

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

        setupOverdueRecyclerView()
        showGreeting()
        displayDashboard()

        binding.tvRecentUsersSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_userManagement)
        }
        binding.tvOverdueReportsSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_reportManagement)
        }
        binding.tvLatestActivitySeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_auditHistory)
        }

        binding.cardTotalUsers.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_userManagement)
        }
        binding.cardTotalReports.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_reportManagement)
        }
        binding.cardOpenReports.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_reportManagement)
        }
        binding.cardOverdueReports.setOnClickListener {
            findNavController().navigate(R.id.action_adminHome_to_reportManagement)
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.hasRole(Role.ADMIN)) {
            displayDashboard()
        }
    }

    private fun setupOverdueRecyclerView() {
        overdueAdapter = OfficerReportAdapter(emptyList()) {
            findNavController().navigate(R.id.action_adminHome_to_reportManagement)
        }
        binding.rvOverdueReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOverdueReports.isNestedScrollingEnabled = false
        binding.rvOverdueReports.adapter = overdueAdapter
    }

    private fun showGreeting() {
        val fullName = SessionManager.currentUser?.fullName ?: ""
        binding.tvGreeting.text = "Hello, ${NameFormat.displayName(fullName)}"
    }

    private fun displayDashboard() {
        binding.tvTotalUsers.text = UserRepository.getAllUsers().size.toString()
        binding.tvTotalReports.text = ReportRepository.getAllReports().size.toString()
        binding.tvOpenReports.text = ReportRepository.getUnresolvedCount().toString()

        val thresholdDays = 7
        val thresholdMillis = thresholdDays * 24L * 60L * 60L * 1000L
        val overdueThreshold = System.currentTimeMillis() - thresholdMillis

        binding.tvOverdueReports.text = ReportRepository.getAllReports().count {
            it.status != ReportStatus.RESOLVED && it.createdAt < overdueThreshold
        }.toString()

        showRecentUsers()
        showOverdueReports(overdueThreshold)
        showLatestActivity()
    }

    private fun showRecentUsers() {
        val users = UserRepository.getAllUsers()
            .sortedByDescending { it.createdAt }
            .take(3)

        binding.layoutRecentUsers.removeAllViews()
        binding.layoutRecentUsers.visibility =
            if (users.isEmpty()) View.GONE else View.VISIBLE
        binding.cardRecentUsersEmpty.visibility =
            if (users.isEmpty()) View.VISIBLE else View.GONE

        users.forEach { user ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                setBackgroundColor(Color.WHITE)
                elevation = 2.dp().toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 10.dp() }
            }

            card.addView(TextView(requireContext()).apply {
                text = user.fullName
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(resources.getColor(R.color.secondary, requireContext().theme))
            })
            card.addView(TextView(requireContext()).apply {
                text = "${user.role.name} · ${if (user.isActive) "Active" else "Inactive"}"
                textSize = 13f
                setTextColor(resources.getColor(
                    if (user.isActive) R.color.primary else android.R.color.holo_red_dark,
                    requireContext().theme
                ))
                setPadding(0, 4.dp(), 0, 0)
            })

            binding.layoutRecentUsers.addView(card)
        }
    }

    private fun showOverdueReports(overdueThreshold: Long) {
        val reports = ReportRepository.getAllReports()
            .filter { it.status != ReportStatus.RESOLVED && it.createdAt < overdueThreshold }
            .sortedBy { it.createdAt }
            .take(3)

        overdueAdapter?.updateReports(reports)
        binding.rvOverdueReports.visibility =
            if (reports.isEmpty()) View.GONE else View.VISIBLE
        binding.cardOverdueReportsEmpty.visibility =
            if (reports.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showLatestActivity() {
        val entries = AuditRepository.getAll().take(3)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        binding.layoutLatestActivity.removeAllViews()
        binding.layoutLatestActivity.visibility =
            if (entries.isEmpty()) View.GONE else View.VISIBLE
        binding.cardLatestActivityEmpty.visibility =
            if (entries.isEmpty()) View.VISIBLE else View.GONE

        entries.forEach { entry ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                setBackgroundColor(Color.WHITE)
                elevation = 2.dp().toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 10.dp() }
            }

            card.addView(TextView(requireContext()).apply {
                text = entry.action.replace('_', ' ')
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(resources.getColor(R.color.secondary, requireContext().theme))
            })
            card.addView(TextView(requireContext()).apply {
                text = entry.details
                textSize = 13f
                setTextColor(Color.DKGRAY)
                setPadding(0, 4.dp(), 0, 0)
            })
            card.addView(TextView(requireContext()).apply {
                text = dateFormat.format(Date(entry.timestamp))
                textSize = 12f
                setTextColor(Color.GRAY)
                setPadding(0, 4.dp(), 0, 0)
            })

            binding.layoutLatestActivity.addView(card)
        }
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        binding.rvOverdueReports.adapter = null
        overdueAdapter = null
        super.onDestroyView()
        _binding = null
    }
}