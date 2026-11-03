package com.group2.cleancity.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.LoginResult
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentLoginBinding
import com.group2.cleancity.model.Role
import com.group2.cleancity.util.Validators

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgotPassword)
        }
        binding.tvSignUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signUp)
        }
    }

    private fun attemptLogin() {
        val identifier = binding.etIdentifier.text.toString()
        val password = binding.etPassword.text.toString()

        // 1. Check the fields
        val idError = Validators.validateIdentifier(identifier)
        val passwordError = Validators.validateRequired(password, "Password")

        binding.tilIdentifier.error = idError
        binding.tilPassword.error = passwordError
        binding.tvLoginError.visibility = View.GONE
        if (idError != null || passwordError != null) return

        // 2. Ask the repository
        when (val result = UserRepository.login(identifier, password)) {
            is LoginResult.Success -> {
                SessionManager.login(result.user)
                goToHome(result.user.role)
            }
            is LoginResult.InvalidCredentials -> showError("Invalid credentials")
            is LoginResult.Deactivated ->
                showError("This account has been deactivated. Contact the administrator.")
        }
    }

    private fun goToHome(role: Role) {
        val action = when (role) {
            Role.RESIDENT -> R.id.action_login_to_residentHome
            Role.OFFICER -> R.id.action_login_to_officerHome
            Role.ADMIN -> R.id.action_login_to_adminHome
        }
        findNavController().navigate(action)
    }

    private fun showError(message: String) {
        binding.tvLoginError.text = message
        binding.tvLoginError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}