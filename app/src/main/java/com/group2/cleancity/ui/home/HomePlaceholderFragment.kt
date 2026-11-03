package com.group2.cleancity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentHomePlaceholderBinding
import com.group2.cleancity.model.Role

// Shared by the three temporary home screens
open class HomePlaceholderFragment(
    private val requiredRole: Role,
    private val title: String
) : Fragment() {

    private var _binding: FragmentHomePlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: only the right role may be here
        if (!SessionManager.hasRole(requiredRole)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        binding.tvTitle.text = title
        binding.tvWelcome.text = "Welcome, ${SessionManager.currentUser?.fullName}"

        binding.btnLogout.setOnClickListener {
            SessionManager.logout()
            findNavController().navigate(R.id.action_global_to_welcome)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}