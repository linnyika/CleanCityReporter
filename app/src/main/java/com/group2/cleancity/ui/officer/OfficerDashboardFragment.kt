package com.group2.cleancity.ui.officer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentOfficerDashboardBinding
import com.group2.cleancity.model.Role

/**
 * Main landing dashboard for Response Officers.
 * Displays key report statistics, officer profile details, and navigation to the report queue.
 */
class OfficerDashboardFragment : Fragment() {

    // View Binding property: holds the reference to generated UI elements in fragment_officer_dashboard.xml
    private var _binding: FragmentOfficerDashboardBinding? = null
    // Non-null accessor for binding that is valid only between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // Called by Android framework to create and inflate the Fragment's layout view tree
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout XML using view binding (replaces findViewById)
        _binding = FragmentOfficerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView when the view hierarchy is fully created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SECURITY ROLE GUARD: Check if the currently logged-in user is an OFFICER
        // If not, block access, display an alert Toast, and redirect back to the Welcome screen
        if (!SessionManager.hasRole(Role.OFFICER)) {
            Toast.makeText(requireContext(), "Unauthorized access: Officer role required", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_to_welcome)
            return // Stop further execution
        }

        // Retrieve current active user session
        val user = SessionManager.currentUser ?: return

        // Populate officer personal profile card on the UI
        binding.tvOfficerWelcome.text = "Welcome back, ${user.fullName}"
        binding.tvOfficerEmail.text = "Email: ${user.email}"
        binding.tvOfficerPhone.text = "Phone: ${user.phone}"

        // Fetch and display real-time dashboard counts from ReportRepository
        refreshDashboardMetrics(user.id)

        // Set click listener on 'View Report Queue' button to open OfficerReportQueueFragment
        binding.btnViewQueue.setOnClickListener {
            findNavController().navigate(R.id.action_officerHome_to_officerQueue)
        }

        // Set click listener on 'Log Out' button to clear user session and clear backstack to Welcome screen
        binding.btnOfficerLogout.setOnClickListener {
            SessionManager.logout() // Clear stored user session in SessionManager
            findNavController().navigate(R.id.action_global_to_welcome) // Navigate to Welcome screen
        }
    }

    // Helper method to fetch calculated metric counts from ReportRepository and update UI text boxes
    private fun refreshDashboardMetrics(officerId: Int) {
        // Incoming: total SUBMITTED reports awaiting initial review
        binding.tvCountIncoming.text = ReportRepository.getSubmittedCount().toString()
        // Assigned: total active (unresolved) reports assigned specifically to this logged-in officer
        binding.tvCountAssigned.text = ReportRepository.getAssignedToOfficerCount(officerId).toString()
        // In Progress: total reports currently being acted upon
        binding.tvCountInProgress.text = ReportRepository.getInProgressCount().toString()
        // Unresolved: total reports in system that are not yet RESOLVED
        binding.tvCountUnresolved.text = ReportRepository.getUnresolvedCount().toString()
        // Overdue: total reports created over 7 days ago that remain unresolved
        binding.tvCountOverdue.text = ReportRepository.getOverdueCount().toString()
    }

    // Lifecycle hook called when the Fragment's view is being destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        // Nullify binding reference to prevent memory leaks when Fragment view is destroyed
        _binding = null
    }
}
