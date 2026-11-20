package com.group2.cleancity.ui.alerts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.NotificationRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentAlertsBinding
import com.group2.cleancity.model.Role

class AlertsFragment : Fragment(R.layout.fragment_alerts) {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private var residentId: Int? = null
    private var alertAdapter: AlertAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlertsBinding.bind(view)

        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }
        residentId = resident.id

        alertAdapter = AlertAdapter { notification ->
            NotificationRepository.markAsRead(notification.id)
            refreshAlerts()

            val arguments = Bundle().apply {
                putString("reportId", notification.reportId)
            }
            findNavController().navigate(
                R.id.action_alerts_to_residentReportDetails,
                arguments
            )
        }

        binding.recyclerAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAlerts.adapter = alertAdapter

        binding.buttonMarkAllRead.setOnClickListener {
            residentId?.let { NotificationRepository.markAllAsRead(it) }
            refreshAlerts()
        }

        refreshAlerts()
    }

    override fun onResume() {
        super.onResume()
        if (residentId != null) refreshAlerts()
    }

    private fun refreshAlerts() {
        val currentResidentId = residentId ?: return
        val adapter = alertAdapter ?: return

        val notifications = NotificationRepository.getForUser(currentResidentId)
        adapter.submitNotifications(notifications)

        val hasNotifications = notifications.isNotEmpty()
        binding.recyclerAlerts.visibility = if (hasNotifications) View.VISIBLE else View.GONE
        binding.layoutEmptyAlerts.visibility = if (hasNotifications) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        binding.recyclerAlerts.adapter = null
        alertAdapter = null
        _binding = null
        super.onDestroyView()
    }
}

