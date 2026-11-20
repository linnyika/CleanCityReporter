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

    private var _binding: FragmentOfficerReportDetailsBinding? = null
    private val binding get() = _binding!!

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

        if (!SessionManager.hasRole(Role.OFFICER)) {
            Toast.makeText(requireContext(), "Unauthorized access: Officer role required", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        reportId = arguments?.getString("reportId")
        if (reportId == null) {
            Toast.makeText(requireContext(), "Report ID missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        loadReportDetails()
    }

    private fun loadReportDetails() {
        val id = reportId ?: return
        val report = ReportRepository.getReportById(id)
        if (report == null) {
            Toast.makeText(requireContext(), "Report not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        val officer = SessionManager.currentUser ?: return

        binding.tvDetailsReportId.text = report.id
        binding.tvDetailsCategory.text = "Category: ${report.categoryName}"
        binding.tvDetailsStatusBadge.text = report.status.name
        binding.tvDetailsPriorityBadge.text = "Priority: ${report.priority.name}"

        binding.tvDetailsReporterName.text = "Name: ${report.reporterName}"
        binding.tvDetailsReporterContact.text = "Contact: ${report.reporterContact}"

        binding.tvDetailsLocation.text = report.locationText
        binding.tvDetailsDescription.text = report.description

        val assigned = report.assignedOfficerName ?: "Unassigned"
        binding.tvDetailsAssignedOfficer.text = "Assigned Officer: $assigned"

        if (report.status == ReportStatus.RESOLVED && !report.resolutionNotes.isNullOrBlank()) {
            binding.cardResolutionDetails.visibility = View.VISIBLE
            binding.tvDetailsResolutionText.text = report.resolutionNotes
        } else {
            binding.cardResolutionDetails.visibility = View.GONE
        }

        binding.btnAcknowledge.isEnabled = (report.status == ReportStatus.SUBMITTED)
        binding.btnMoveInProgress.isEnabled = (report.status == ReportStatus.ACKNOWLEDGED)
        binding.btnResolveReport.isEnabled = (report.status == ReportStatus.IN_PROGRESS)

        binding.btnAcknowledge.setOnClickListener {
            ReportRepository.acknowledgeReport(report.id, officer)
            Toast.makeText(requireContext(), "Report acknowledged!", Toast.LENGTH_SHORT).show()
            loadReportDetails()
        }

        binding.btnMoveInProgress.setOnClickListener {
            val err = ReportRepository.updateStatus(report.id, ReportStatus.IN_PROGRESS, officer, null)
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Status updated to IN_PROGRESS", Toast.LENGTH_SHORT).show()
                loadReportDetails()
            }
        }

        binding.btnResolveReport.setOnClickListener {
            showResolutionDialog(report)
        }

        binding.btnAssignOfficer.setOnClickListener {
            showAssignOfficerDialog(report)
        }

        binding.btnChangePriority.setOnClickListener {
            showChangePriorityDialog(report)
        }

        refreshInternalNotes(report)
        binding.btnAddInternalNote.setOnClickListener {
            val noteText = binding.etInternalNote.text?.toString()?.trim() ?: ""
            if (noteText.isEmpty()) {
                Toast.makeText(requireContext(), "Note text cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                ReportRepository.addInternalNote(report.id, noteText, officer)
                binding.etInternalNote.setText("")
                Toast.makeText(requireContext(), "Internal note saved", Toast.LENGTH_SHORT).show()
                refreshInternalNotes(report)
            }
        }
    }

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
                if (notes.isEmpty()) {
                    Toast.makeText(requireContext(), "Resolution details are required to resolve a report!", Toast.LENGTH_LONG).show()
                } else {
                    val err = ReportRepository.updateStatus(report.id, ReportStatus.RESOLVED, officer, notes)
                    if (err != null) {
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Report marked as RESOLVED", Toast.LENGTH_SHORT).show()
                        loadReportDetails()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

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

    private fun refreshInternalNotes(report: Report) {
        if (report.internalNotes.isEmpty()) {
            binding.tvInternalNotesList.text = "No internal notes recorded yet."
        } else {
            binding.tvInternalNotesList.text = report.internalNotes.joinToString("\n\n")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}