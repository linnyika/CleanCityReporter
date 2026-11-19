package com.group2.cleancity.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.SettingsRepository
import com.group2.cleancity.databinding.FragmentSystemSettingsBinding
import com.group2.cleancity.model.Role

class SystemSettingsFragment : Fragment() {

    private var _binding: FragmentSystemSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemSettingsBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            Toast.makeText(
                requireContext(),
                "Administrator access required",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.etOverdueThreshold.setText(
            SettingsRepository.getOverdueThreshold().toString()
        )

        binding.btnSaveSettings.setOnClickListener {
            val days = binding.etOverdueThreshold.text.toString().toIntOrNull()

            if (days == null) {
                binding.etOverdueThreshold.error = "Enter a valid number"
                return@setOnClickListener
            }

            val error = SettingsRepository.updateOverdueThreshold(days)

            if (error != null) {
                binding.etOverdueThreshold.error = error
            } else {
                Toast.makeText(
                    requireContext(),
                    "Settings saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
