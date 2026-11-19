package com.group2.cleancity.ui.admin

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group2.cleancity.R
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.data.UserRepository
import com.group2.cleancity.databinding.FragmentUserManagementBinding
import com.group2.cleancity.model.Role
import com.group2.cleancity.model.User

class UserManagementFragment : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            findNavController().navigate(R.id.action_global_to_welcome)
            return
        }

        binding.btnAddOfficer.setOnClickListener {
            showCreateOfficerDialog()
        }

        displayUsers()
    }

    private fun showCreateOfficerDialog() {
        val form = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp(), 8.dp(), 24.dp(), 0)
        }

        val nameInput = createInput(form, "Full name")
        val emailInput = createInput(form, "Official staff email")
        val phoneInput = createInput(form, "Phone number")
        val passwordInput = createInput(form, "Temporary password").apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Officer Account")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val error = UserRepository.createOfficer(
                    fullName = nameInput.text.toString(),
                    email = emailInput.text.toString(),
                    phone = phoneInput.text.toString(),
                    password = passwordInput.text.toString()
                )

                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Officer account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                    displayUsers()
                }
            }
        }

        dialog.show()
    }

    private fun createInput(container: LinearLayout, hintText: String): EditText {
        val input = EditText(requireContext()).apply {
            hint = hintText
            textSize = 15f
            setSingleLine(true)
        }

        container.addView(
            input,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        return input
    }

    private fun displayUsers() {
        val users = UserRepository.getAllUsers()

        binding.tvUserCount.text = "Registered users: ${users.size}"
        binding.layoutUsers.removeAllViews()

        binding.tvNoUsers.visibility =
            if (users.isEmpty()) View.VISIBLE else View.GONE

        users.forEach { user ->
            binding.layoutUsers.addView(createUserCard(user))
        }
    }


    private fun createUserCard(user: User): LinearLayout {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 14.dp().toFloat()
                setStroke(1.dp(), Color.LTGRAY)
            }

            elevation = 2.dp().toFloat()

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12.dp()
            }
        }

        card.addView(createTextView(user.fullName, 18f, true, R.color.secondary))
        card.addView(createTextView(user.email, 14f, false))
        card.addView(createTextView("Phone: ${user.phone}", 14f, false))
        card.addView(createTextView("Role: ${user.role.name}", 14f, true, R.color.primary))

        val status = if (user.isActive) "Active" else "Inactive"
        val statusColor = if (user.isActive) R.color.primary
        else android.R.color.holo_red_dark

        card.addView(
            createTextView("Account status: $status", 14f, true, statusColor)
        )

        // Account management buttons
        val actions = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10.dp(), 0, 0)
        }

        val statusButton = android.widget.Button(requireContext()).apply {
            text = if (user.isActive) "Deactivate" else "Reactivate"
            textSize = 12f
            isAllCaps = false

            setOnClickListener {
                val adminId = SessionManager.currentUser?.id ?: return@setOnClickListener

                val newStatus = !user.isActive

                val error = UserRepository.updateUserStatus(
                    userId = user.id,
                    isActive = newStatus,
                    actingAdminId = adminId
                )

                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        if (newStatus) "Account reactivated" else "Account deactivated",
                        Toast.LENGTH_SHORT
                    ).show()

                    displayUsers()
                }
            }
        }

        val roleButton = android.widget.Button(requireContext()).apply {
            text = "Change Role"
            textSize = 12f
            isAllCaps = false

            setOnClickListener {
                showChangeRoleDialog(user)
            }
        }

        actions.addView(
            statusButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        actions.addView(
            roleButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        card.addView(actions)

        return card
    }


    private fun showChangeRoleDialog(user: User) {
        val roles = arrayOf(
            Role.RESIDENT,
            Role.OFFICER,
            Role.ADMIN
        )

        val roleLabels = arrayOf(
            "Resident",
            "Officer",
            "Administrator"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Change role for ${user.fullName}")
            .setItems(roleLabels) { _, selectedIndex ->

                val adminId = SessionManager.currentUser?.id
                    ?: return@setItems

                val selectedRole = roles[selectedIndex]

                val error = UserRepository.updateUserRole(
                    userId = user.id,
                    newRole = selectedRole,
                    actingAdminId = adminId
                )

                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "User role updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    displayUsers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun createTextView(
        value: String,
        size: Float,
        bold: Boolean = false,
        colorResource: Int = android.R.color.darker_gray
    ): TextView {
        return TextView(requireContext()).apply {
            text = value
            textSize = size
            setTextColor(ContextCompat.getColor(requireContext(), colorResource))
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, 4.dp(), 0, 4.dp())
        }
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
