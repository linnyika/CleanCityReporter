package com.group2.cleancity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentResidentHomeBinding
import com.group2.cleancity.model.Role

class ResidentHomeFragment : Fragment() {

    private var _binding: FragmentResidentHomeBinding? = null
    private val binding get() = _binding!!

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

        showGreeting()
        showCounts()

        binding.cardReport.setOnClickListener {
            // TODO (Slice 3): navigate to the Incident Logger form
            Toast.makeText(requireContext(), "Report form coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGreeting() {
        val fullName = SessionManager.currentUser?.fullName ?: ""
        // "Demo Resident" -> "Demo"
        val firstName = fullName.trim().substringBefore(" ")
        binding.tvGreeting.text = "Hello, $firstName"
    }

    private fun showCounts() {
        // TODO (Slice 4): replace these zeros with counts from ReportRepository
        binding.tvCountTotal.text = "0"
        binding.tvCountInProgress.text = "0"
        binding.tvCountResolved.text = "0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}