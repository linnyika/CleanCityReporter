package com.group2.cleancity.ui.officer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentOfficerReportDetailsBinding
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role

/**
 * Detailed view for Officers to review reports, assign officers, transition statuses,
 * input required resolution details, and add internal staff notes.
 */
class OfficerReportDetailsFragment : Fragment() {

    // View Binding property holding layout references for fragment_officer_report_details.xml
    private var _binding: FragmentOfficerReportDetailsBinding? = null
    private val binding get() = _binding!!

    // Holds the unique report ID string (e.g., "NRB-0001") passed via Navigation Safe Args
    private var reportId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficerReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SECURITY ROLE GUARD: Ensure user is logged in as OFFICER
        if (!SessionManager.hasRole(Role.OFFICER)) {
            Toast.makeText(requireContext(), "Unauthorized access: Officer role required", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_to_welcome)
            return // Block execution
        }

        // Retrieve reportId parameter from Fragment arguments Bundle
        reportId = arguments?.getString("reportId")
        if (reportId == null) {
            Toast.makeText(requireContext(), "Report ID missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp() // Return to previous screen
            return
        }

        // Load report object and configure UI buttons and views
        loadReportDetails()
    }

    // Fetches report details from ReportRepository and binds them to UI widgets
    private fun loadReportDetails() {
        val id = reportId ?: return
        val report = ReportRepository.getReportById(id)
        if (report == null) {
            Toast.makeText(requireContext(), "Report not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        val officer = SessionManager.currentUser ?: return

        // Populate basic report fields
        binding.tvDetailsReportId.text = report.id
        binding.tvDetailsCategory.text = "Category: ${report.categoryName}"
        binding.tvDetailsStatusBadge.text = report.status.name
        binding.tvDetailsPriorityBadge.text = "Priority: ${report.priority.name}"

        // Populate officer-exclusive reporter contact details
        binding.tvDetailsReporterName.text = "Name: ${report.reporterName}"
        binding.tvDetailsReporterContact.text = "Contact: ${report.reporterContact}"

        // Populate location and description text
        binding.tvDetailsLocation.text = report.locationText
        binding.tvDetailsDescription.text = report.description

        // Display assigned officer name or "Unassigned"
        val assigned = report.assignedOfficerName ?: "Unassigned"
        binding.tvDetailsAssignedOfficer.text = "Assigned Officer: $assigned"

        // Handle Resolution Details Card visibility (visible only when status is RESOLVED)
        if (report.status == ReportStatus.RESOLVED && !report.resolutionNotes.isNullOrBlank()) {
            binding.cardResolutionDetails.visibility = View.VISIBLE
            binding.tvDetailsResolutionText.text = report.resolutionNotes
        } else {
            binding.cardResolutionDetails.visibility = View.GONE
        }

        // ENFORCE STRICT SEQUENTIAL WORKFLOW IN BUTTON STATES:
        // 1. Acknowledge button is enabled ONLY when status is SUBMITTED
        binding.btnAcknowledge.isEnabled = (report.status == ReportStatus.SUBMITTED)
        // 2. Move to In Progress button is enabled ONLY when status is ACKNOWLEDGED
        binding.btnMoveInProgress.isEnabled = (report.status == ReportStatus.ACKNOWLEDGED)
        // 3. Resolve button is enabled ONLY when status is IN_PROGRESS
        binding.btnResolveReport.isEnabled = (report.status == ReportStatus.IN_PROGRESS)

        // Button Click Listener: Acknowledge report (SUBMITTED -> ACKNOWLEDGED)
        binding.btnAcknowledge.setOnClickListener {
            ReportRepository.acknowledgeReport(report.id, officer)
            Toast.makeText(requireContext(), "Report acknowledged!", Toast.LENGTH_SHORT).show()
            loadReportDetails() // Refresh UI state
        }

        // Button Click Listener: Move status to IN_PROGRESS (ACKNOWLEDGED -> IN_PROGRESS)
        binding.btnMoveInProgress.setOnClickListener {
            val err = ReportRepository.updateStatus(report.id, ReportStatus.IN_PROGRESS, officer, null)
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Status updated to IN_PROGRESS", Toast.LENGTH_SHORT).show()
                loadReportDetails() // Refresh UI state
            }
        }

        // Button Click Listener: Resolve report (Prompts for mandatory resolution details)
        binding.btnResolveReport.setOnClickListener {
            showResolutionDialog(report)
        }

        // Button Click Listener: Open officer assignment dialog
        binding.btnAssignOfficer.setOnClickListener {
            showAssignOfficerDialog(report)
        }

        // Button Click Listener: Open priority change dialog
        binding.btnChangePriority.setOnClickListener {
            showChangePriorityDialog(report)
        }

        // Load internal notes history and setup add note listener
        refreshInternalNotes(report)
        binding.btnAddInternalNote.setOnClickListener {
            val noteText = binding.etInternalNote.text?.toString()?.trim() ?: ""
            if (noteText.isEmpty()) {
                Toast.makeText(requireContext(), "Note text cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                ReportRepository.addInternalNote(report.id, noteText, officer)
                binding.etInternalNote.setText("") // Clear input text box
                Toast.makeText(requireContext(), "Internal note saved", Toast.LENGTH_SHORT).show()
                refreshInternalNotes(report) // Refresh notes list
            }
        }
    }

    // Displays an AlertDialog prompting the officer for required resolution details before marking RESOLVED
    private fun showResolutionDialog(report: Report) {
        val officer = SessionManager.currentUser ?: return
        val input = EditText(requireContext())
        input.hint = "Enter resolution details (Required)"

        AlertDialog.Builder(requireContext())
            .setTitle("Resolve Report ${report.id}")
            .setMessage("Please provide mandatory details on how this sanitation issue was resolved:")
            .setView(input)
            .setPositiveButton("Resolve") { _, _ ->
                val notes = input.text.toString().trim()
                // BUSINESS RULE CHECK: Block resolution if details are blank
                if (notes.isEmpty()) {
                    Toast.makeText(requireContext(), "Resolution details are required to resolve a report!", Toast.LENGTH_LONG).show()
                } else {
                    val err = ReportRepository.updateStatus(report.id, ReportStatus.RESOLVED, officer, notes)
                    if (err != null) {
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Report marked as RESOLVED", Toast.LENGTH_SHORT).show()
                        loadReportDetails() // Refresh UI
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Displays an AlertDialog allowing assignment of the report to an active officer
    private fun showAssignOfficerDialog(report: Report) {
        val currentOfficer = SessionManager.currentUser ?: return
        val officers = UserRepository.getOfficers()
        if (officers.isEmpty()) {
            Toast.makeText(requireContext(), "No active officers available", Toast.LENGTH_SHORT).show()
            return
        }
        val names = officers.map { it.fullName }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Assign Officer")
            .setItems(names) { _, which ->
                val selectedOfficer = officers[which]
                ReportRepository.assignOfficer(report.id, selectedOfficer.id, selectedOfficer.fullName, currentOfficer)
                Toast.makeText(requireContext(), "Assigned to ${selectedOfficer.fullName}", Toast.LENGTH_SHORT).show()
                loadReportDetails()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Displays an AlertDialog allowing change of priority level
    private fun showChangePriorityDialog(report: Report) {
        val currentOfficer = SessionManager.currentUser ?: return
        val priorities = Priority.values()
        val names = priorities.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Change Priority")
            .setItems(names) { _, which ->
                val selectedPriority = priorities[which]
                ReportRepository.updatePriority(report.id, selectedPriority, currentOfficer)
                Toast.makeText(requireContext(), "Priority updated to ${selectedPriority.name}", Toast.LENGTH_SHORT).show()
                loadReportDetails()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Formats and displays internal staff notes in the UI
    private fun refreshInternalNotes(report: Report) {
        if (report.internalNotes.isEmpty()) {
            binding.tvInternalNotesList.text = "No internal notes recorded yet."
        } else {
            binding.tvInternalNotesList.text = report.internalNotes.joinToString("\n\n")
        }
    }

    // Clean up binding reference on view destruction
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
