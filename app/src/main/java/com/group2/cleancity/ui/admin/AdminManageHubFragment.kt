package com.group2.cleancity.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentAdminManageHubBinding
import com.group2.cleancity.model.Role

class AdminManageHubFragment : Fragment() {

    private var _binding: FragmentAdminManageHubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        binding.cardManageUsers.setOnClickListener {
            findNavController().navigate(R.id.userManagementFragment)
        }

        binding.cardManageCategories.setOnClickListener {
            findNavController().navigate(R.id.categoryManagementFragment)
        }

        binding.cardSystemSettings.setOnClickListener {
            findNavController().navigate(R.id.systemSettingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}