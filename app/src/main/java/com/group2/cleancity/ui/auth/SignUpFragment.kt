package com.group2.cleancity.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentSignUpBinding
import com.group2.cleancity.util.Validators

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateAccount.setOnClickListener { attemptSignUp() }
        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_login)
        }
        binding.tvTermsLink.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_terms)
        }
    }

    private fun attemptSignUp() {
        val fullName = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirm.text.toString()

        // 1. Check every field and show each error under its own field
        val nameError = Validators.validateFullName(fullName)
        val emailError = Validators.validateResidentEmail(email)
        val phoneError = Validators.validatePhone(phone)
        val passwordError = Validators.validatePassword(password)
        val confirmError = Validators.validateConfirmPassword(password, confirm)
        val termsAccepted = binding.cbTerms.isChecked

        binding.tilFullName.error = nameError
        binding.tilEmail.error = emailError
        binding.tilPhone.error = phoneError
        binding.tilPassword.error = passwordError
        binding.tilConfirm.error = confirmError
        binding.tvTermsError.visibility = if (termsAccepted) View.GONE else View.VISIBLE

        val hasErrors = listOf(nameError, emailError, phoneError, passwordError, confirmError)
            .any { it != null } || !termsAccepted
        if (hasErrors) return

        // 2. Ask the repository to save (it checks duplicates and the staff-domain rule)
        val repoError = UserRepository.registerResident(fullName, email, phone, password)

        if (repoError != null) {
            // Put the message under the field it is about
            if (repoError.contains("phone", ignoreCase = true)) {
                binding.tilPhone.error = repoError
            } else {
                binding.tilEmail.error = repoError
            }
            return
        }

        // 3. Success: tell the user and go to Login
        Toast.makeText(requireContext(), "Account created. Please log in.", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_signUp_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}