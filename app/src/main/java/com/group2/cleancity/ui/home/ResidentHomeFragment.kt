package com.group2.cleancity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentResidentHomeBinding
import com.group2.cleancity.model.Role
import com.group2.cleancity.ui.report.MyReportAdapter

class ResidentHomeFragment : Fragment() {

    private var _binding: FragmentResidentHomeBinding? = null
    private val binding get() = _binding!!

    private var reportAdapter: MyReportAdapter? = null
    private val viewModel: ResidentHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: only a signed-in Resident may be here
        if (!SessionManager.hasRole(Role.RESIDENT)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        setupRecyclerView()
        observeViewModel()
        viewModel.loadResidentData()

        // Slice 3: open the report form (green card and the + button)
        binding.cardReport.setOnClickListener {
            findNavController().navigate(R.id.action_residentHome_to_report)
        }
        binding.fabReport.setOnClickListener {
            findNavController().navigate(R.id.action_residentHome_to_report)
        }

        // Navigate to My Reports screen (same as bottom nav tab)
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }
    }

    private fun setupRecyclerView() {
        reportAdapter = MyReportAdapter { reportId ->
            val arguments = Bundle().apply { putString("reportId", reportId) }
            findNavController().navigate(R.id.residentReportDetailsFragment, arguments)
        }
        binding.recyclerRecentReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentReports.adapter = reportAdapter
    }

    private fun observeViewModel() {
        viewModel.greeting.observe(viewLifecycleOwner) { greetingText ->
            binding.tvGreeting.text = greetingText
        }
        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.tvCountTotal.text = count
        }
        viewModel.inProgressCount.observe(viewLifecycleOwner) { count ->
            binding.tvCountInProgress.text = count
        }
        viewModel.resolvedCount.observe(viewLifecycleOwner) { count ->
            binding.tvCountResolved.text = count
        }
        viewModel.recentReports.observe(viewLifecycleOwner) { recentReports ->
            val adapter = reportAdapter ?: return@observe
            adapter.submitReports(recentReports)

            val hasReports = recentReports.isNotEmpty()
            binding.recyclerRecentReports.visibility = if (hasReports) View.VISIBLE else View.GONE
            binding.tvRecentEmpty.visibility = if (hasReports) View.GONE else View.VISIBLE
        }
    }

    // Refresh the numbers and recent reports when returning to this screen
    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.isLoggedIn) {
            viewModel.loadResidentData()
        }
    }

    private fun updateDashboard() {
        viewModel.loadResidentData()
    }

    private fun showGreeting() {
        viewModel.loadResidentData()
    }

    private fun showCounts() {
        viewModel.loadResidentData()
    }

    private fun showRecentReports() {
        viewModel.loadResidentData()
    }

    override fun onDestroyView() {
        binding.recyclerRecentReports.adapter = null
        reportAdapter = null
        super.onDestroyView()
        _binding = null
    }
}
