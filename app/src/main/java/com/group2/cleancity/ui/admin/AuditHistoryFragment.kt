package com.group2.cleancity.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group2.cleancity.R
import com.group2.cleancity.data.AuditRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentAuditHistoryBinding
import com.group2.cleancity.model.Role
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditHistoryFragment : Fragment() {

    private var _binding: FragmentAuditHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditHistoryBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            Toast.makeText(
                requireContext(),
                "Access denied. Admins only.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        displayAuditEntries()
    }

    private fun displayAuditEntries() {
        if (_binding == null) return

        val entries = AuditRepository.getAll()

        binding.tvAuditCount.text = "Total activities: ${entries.size}"
        binding.auditListContainer.removeAllViews()

        binding.tvNoAuditEntries.visibility =
            if (entries.isEmpty()) View.VISIBLE else View.GONE

        entries.forEach { entry ->

            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(android.R.color.white)
                setPadding(20, 16, 20, 16)

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                layoutParams = params
            }

            val actionView = TextView(requireContext()).apply {
                text = entry.action.replace('_', ' ')
                textSize = 17f
                setTextColor(
                    resources.getColor(
                        R.color.secondary,
                        requireContext().theme
                    )
                )
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val detailsView = TextView(requireContext()).apply {
                text = entry.details
                textSize = 14f
                setTextColor(
                    resources.getColor(
                        android.R.color.black,
                        requireContext().theme
                    )
                )
                setPadding(0, 8, 0, 8)
            }

            val userView = TextView(requireContext()).apply {
                text = "Performed by: ${entry.performedByName} (ID: ${entry.performedByUserId})"
                textSize = 13f
                setTextColor(
                    resources.getColor(
                        R.color.secondary,
                        requireContext().theme
                    )
                )
            }

            val dateFormat = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )

            val dateView = TextView(requireContext()).apply {
                text = dateFormat.format(Date(entry.timestamp))
                textSize = 12f
                setTextColor(
                    resources.getColor(
                        android.R.color.darker_gray,
                        requireContext().theme
                    )
                )
                setPadding(0, 6, 0, 0)
            }

            card.addView(actionView)
            card.addView(detailsView)
            card.addView(userView)
            card.addView(dateView)

            binding.auditListContainer.addView(card)
        }
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null && SessionManager.hasRole(Role.ADMIN)) {
            displayAuditEntries()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
