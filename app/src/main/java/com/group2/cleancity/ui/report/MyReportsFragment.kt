package com.group2.cleancity.ui.report

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentMyReportsBinding
import com.group2.cleancity.model.Role

/** Shows the signed-in resident's own report history. */
class MyReportsFragment : Fragment(R.layout.fragment_my_reports) {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!

    private var reportAdapter: MyReportAdapter? = null
    private var residentId: Int? = null
    private val viewModel: MyReportsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyReportsBinding.bind(view)

        // The stored account supplies both the role and the resident ID.
        val resident = SessionManager.currentUser
        if (resident == null || resident.role != Role.RESIDENT) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }
        residentId = resident.id

        reportAdapter = MyReportAdapter { reportId ->
            val arguments = Bundle().apply { putString("reportId", reportId) }
            findNavController().navigate(R.id.residentReportDetailsFragment, arguments)
        }

        binding.recyclerMyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyReports.adapter = reportAdapter

        observeViewModel()

        binding.buttonSubmitFirstReport.setOnClickListener {
            findNavController().navigate(R.id.reportFragment)
        }

        binding.etSearchReports.doAfterTextChanged { text ->
            val query = text?.toString().orEmpty()
            val currentResidentId = residentId ?: return@doAfterTextChanged
            viewModel.setSearchQuery(query, currentResidentId)
        }

        binding.filterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val filter = when (checkedId) {
                R.id.buttonFilterSubmitted -> MyReportsViewModel.ReportFilter.SUBMITTED
                R.id.buttonFilterInProgress -> MyReportsViewModel.ReportFilter.IN_PROGRESS
                R.id.buttonFilterResolved -> MyReportsViewModel.ReportFilter.RESOLVED
                else -> MyReportsViewModel.ReportFilter.ALL
            }
            val currentResidentId = residentId ?: return@addOnButtonCheckedListener
            viewModel.setFilter(filter, currentResidentId)
        }

        viewModel.loadReports(resident.id)
    }

    private fun observeViewModel() {
        viewModel.visibleReports.observe(viewLifecycleOwner) { visibleReports ->
            val adapter = reportAdapter ?: return@observe
            adapter.submitReports(visibleReports)

            val hasReports = visibleReports.isNotEmpty()
            binding.recyclerMyReports.visibility = if (hasReports) View.VISIBLE else View.GONE
            binding.layoutEmptyReports.visibility = if (hasReports) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh in case the resident submitted a report and returned here.
        residentId?.let { viewModel.loadReports(it) }
    }

    override fun onDestroyView() {
        binding.recyclerMyReports.adapter = null
        reportAdapter = null
        _binding = null
        super.onDestroyView()
    }
}
