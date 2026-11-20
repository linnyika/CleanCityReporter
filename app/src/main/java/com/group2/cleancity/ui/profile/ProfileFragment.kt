package com.group2.cleancity.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.group2.cleancity.R
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentProfileBinding
import com.group2.cleancity.model.ReportStatus
import com.group2.cleancity.model.Role
import com.group2.cleancity.util.Validators

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Guard: check if any user is logged in
        if (!SessionManager.isLoggedIn) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        showCurrentDetails()
        setupRoleSpecificViews()
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnChangePassword.setOnClickListener { changePassword() }
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && SessionManager.isLoggedIn) {
            showCurrentDetails()
            setupRoleSpecificViews()
        }
    }

    private fun setupRoleSpecificViews() {
        val user = SessionManager.currentUser ?: return
        if (user.role == Role.RESIDENT) {
            binding.cardImpact.visibility = View.VISIBLE
            showImpact()
        } else {
            binding.cardImpact.visibility = View.GONE
        }
    }

    private fun showImpact() {
        val user = SessionManager.currentUser ?: return
        if (user.role != Role.RESIDENT) return

        val myReports = ReportRepository.getReportsByReporter(user.id)
        val submitted = myReports.size
        val resolved = myReports.count { it.status == ReportStatus.RESOLVED }

        binding.tvImpactSubmitted.text = submitted.toString()
        binding.tvImpactResolved.text = resolved.toString()
        binding.tvImpactLevel.text = "Level: ${participationLevel(submitted)}"
    }

    // Turns a report count into a level name based on participation thresholds.
    private fun participationLevel(reportCount: Int): String = when {
        reportCount >= 6 -> "Champion"
        reportCount in 3..5 -> "Active"
        reportCount in 1..2 -> "Starter"
        else -> "Newcomer"
    }

    private fun showCurrentDetails() {
        val user = SessionManager.currentUser ?: return
        binding.tvRole.text = user.role.name
        binding.tvEmail.text = user.email
        binding.etFullName.setText(user.fullName)
        binding.etPhone.setText(user.phone)
    }

    private fun saveProfile() {
        val user = SessionManager.currentUser ?: return
        val name = binding.etFullName.text.toString()
        val phone = binding.etPhone.text.toString()

        // Same rules as Sign Up; each error appears under its own field
        val nameError = Validators.validateFullName(name)
        val phoneError = Validators.validatePhone(phone)
        binding.tilFullName.error = nameError
        binding.tilPhone.error = phoneError
        if (nameError != null || phoneError != null) return

        // The repository does the saving (and checks the phone isn't taken)
        val saveError = UserRepository.updateProfile(user.id, name, phone)
        if (saveError != null) {
            binding.tilPhone.error = saveError
            return
        }

        // Keep the session in sync so Home shows the new name
        UserRepository.findById(user.id)?.let { SessionManager.refresh(it) }
        showCurrentDetails()
        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
    }

    private fun changePassword() {
        val user = SessionManager.currentUser ?: return
        val current = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        // 1. Check the form first, each error under its own field
        val currentError = if (current.isEmpty()) "Enter your current password" else null
        val newError = Validators.validatePassword(newPass)
        val confirmError = Validators.validateConfirmPassword(newPass, confirm)
        binding.tilCurrentPassword.error = currentError
        binding.tilNewPassword.error = newError
        binding.tilConfirmPassword.error = confirmError
        if (currentError != null || newError != null || confirmError != null) return

        // 2. The repository checks the current password and saves the new hash
        val saveError = UserRepository.changePassword(user.id, current, newPass)
        if (saveError != null) {
            // Wrong current password, or new equals old
            binding.tilCurrentPassword.error = saveError
            return
        }

        // 3. Keep the session in sync, then clear the fields
        UserRepository.findById(user.id)?.let { SessionManager.refresh(it) }
        binding.etCurrentPassword.text?.clear()
        binding.etNewPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
        Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT).show()
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log out")
            .setMessage("Log out of Clean City Reporter?")
            .setNegativeButton("Cancel", null)   // null = just close the dialog
            .setPositiveButton("Log Out") { _, _ -> logout() }
            .show()
    }

    private fun logout() {
        SessionManager.logout()                                  // 1. forget the user
        findNavController().navigate(R.id.action_global_to_welcome)  // 2. clear back stack, go to Welcome
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}