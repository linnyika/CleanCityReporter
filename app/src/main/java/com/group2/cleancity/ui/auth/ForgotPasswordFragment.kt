package com.group2.cleancity.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.databinding.FragmentForgotPasswordBinding
import com.group2.cleancity.util.Validators

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendReset.setOnClickListener { sendResetLink() }
        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendResetLink() {
        val identifier = binding.etIdentifier.text.toString()

        val error = Validators.validateIdentifier(identifier)
        binding.tilIdentifier.error = error

        if (error != null) {
            binding.tvResetMessage.visibility = View.GONE
            return
        }

        // Simulated: nothing is actually sent in the MVP.
        // The same message is shown whether or not the account exists,
        // so nobody can use this screen to discover registered emails.
        binding.tvResetMessage.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}