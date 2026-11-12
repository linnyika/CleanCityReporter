package com.group2.cleancity.ui.officer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentOfficerReportQueueBinding
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role

/**
 * Fragment displaying the filterable & searchable report queue for Officers.
 */
class OfficerReportQueueFragment : Fragment() {

    // View Binding property holding references to UI elements in fragment_officer_report_queue.xml
    private var _binding: FragmentOfficerReportQueueBinding? = null
    private val binding get() = _binding!!

    // RecyclerView Adapter instance responsible for rendering report list cards
    private lateinit var adapter: OfficerReportAdapter

    // Inflates the XML layout using view binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficerReportQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called after UI view hierarchy is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SECURITY ROLE GUARD: Ensure user is logged in as OFFICER
        if (!SessionManager.hasRole(Role.OFFICER)) {
            Toast.makeText(requireContext(), "Unauthorized access: Officer role required", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_to_welcome)
            return // Block execution
        }

        // Initialize RecyclerView, adapters, and layout manager
        setupRecyclerView()

        // Populate dropdown spinners with filter choices (Status & Priority)
        setupFilterSpinners()

        // Attach text change listener to search box for real-time filtering
        setupSearchInput()

        // Initial fetch and display of reports matching current filters
        applyFilters()
    }

    // Configures RecyclerView adapter and click navigation handling
    private fun setupRecyclerView() {
        // Instantiate custom adapter with item click callback lambda
        adapter = OfficerReportAdapter(emptyList()) { selectedReport ->
            // Pass the selected report's unique ID as a navigation argument Bundle
            val bundle = bundleOf("reportId" to selectedReport.id)
            // Navigate from queue fragment to details fragment
            findNavController().navigate(R.id.action_officerQueue_to_officerDetails, bundle)
        }
        // Set a standard vertical linear layout manager for RecyclerView
        binding.rvOfficerReports.layoutManager = LinearLayoutManager(requireContext())
        // Attach adapter to RecyclerView
        binding.rvOfficerReports.adapter = adapter
    }

    // Configures drop-down Spinners for filtering by status, category, and priority
    private fun setupFilterSpinners() {
        // Build Status filter options ("All Statuses" + SUBMITTED, ACKNOWLEDGED, etc.)
        val statusOptions = mutableListOf("All Statuses")
        statusOptions.addAll(ReportStatus.values().map { it.name })
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatusFilter.adapter = statusAdapter

        // Build Category filter options ("All Categories" + categories present in reports)
        val categoryOptions = mutableListOf("All Categories")
        categoryOptions.addAll(ReportRepository.getAllReports().map { it.categoryName }.distinct())
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryOptions)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = categoryAdapter

        // Build Priority filter options ("All Priorities" + LOW, MEDIUM, HIGH, URGENT)
        val priorityOptions = mutableListOf("All Priorities")
        priorityOptions.addAll(Priority.values().map { it.name })
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorityOptions)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriorityFilter.adapter = priorityAdapter

        // Shared item selection listener: triggers filter re-application whenever selection changes
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerStatusFilter.onItemSelectedListener = listener
        binding.spinnerCategoryFilter.onItemSelectedListener = listener
        binding.spinnerPriorityFilter.onItemSelectedListener = listener
    }

    // Listens for keystrokes in the location/keyword search box
    private fun setupSearchInput() {
        binding.etSearchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            // Called automatically whenever text is typed or cleared
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters() // Re-filter report list in real time
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Core filtering & sorting logic combining text search, status dropdown, category dropdown, and priority dropdown
    private fun applyFilters() {
        // Step 1: Start with all reports from the repository
        var reports = ReportRepository.getAllReports()

        // Step 2: Apply text query matching against location, report ID, category, or description
        val query = binding.etSearchLocation.text?.toString()?.trim() ?: ""
        if (query.isNotEmpty()) {
            reports = reports.filter {
                it.locationText.contains(query, ignoreCase = true) ||
                        it.id.contains(query, ignoreCase = true) ||
                        it.categoryName.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // Step 3: Apply selected Status dropdown filter (if not "All Statuses")
        val selectedStatusIndex = binding.spinnerStatusFilter.selectedItemPosition
        if (selectedStatusIndex > 0) {
            val statusName = binding.spinnerStatusFilter.selectedItem.toString()
            reports = reports.filter { it.status.name == statusName }
        }

        // Step 4: Apply selected Category dropdown filter (if not "All Categories")
        val selectedCategoryIndex = binding.spinnerCategoryFilter.selectedItemPosition
        if (selectedCategoryIndex > 0) {
            val categoryName = binding.spinnerCategoryFilter.selectedItem.toString()
            reports = reports.filter { it.categoryName == categoryName }
        }

        // Step 5: Apply selected Priority dropdown filter (if not "All Priorities")
        val selectedPriorityIndex = binding.spinnerPriorityFilter.selectedItemPosition
        if (selectedPriorityIndex > 0) {
            val priorityName = binding.spinnerPriorityFilter.selectedItem.toString()
            reports = reports.filter { it.priority.name == priorityName }
        }

        // Step 5: Sort filtered reports by creation date (newest first)
        reports = reports.sortedByDescending { it.createdAt }

        // Step 6: Pass filtered list to RecyclerView adapter
        adapter.updateReports(reports)

        // Step 7: Handle empty state view visibility
        if (reports.isEmpty()) {
            binding.tvQueueEmpty.visibility = View.VISIBLE
            binding.rvOfficerReports.visibility = View.GONE
        } else {
            binding.tvQueueEmpty.visibility = View.GONE
            binding.rvOfficerReports.visibility = View.VISIBLE
        }
    }

    // Clean up View Binding reference on view destruction
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
