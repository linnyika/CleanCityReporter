package com.group2.cleancity.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group2.cleancity.data.CategoryRepository
import com.group2.cleancity.data.SessionManager
import com.group2.cleancity.databinding.FragmentCategoryManagementBinding
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Role

class CategoryManagementFragment : Fragment() {

    private var _binding: FragmentCategoryManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManagementBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!SessionManager.hasRole(Role.ADMIN)) {
            return
        }

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }

        displayCategories()
    }

    private fun addCategory() {
        val name = binding.etCategoryName.text.toString()
        val description = binding.etCategoryDescription.text.toString()
        val priority = when (binding.spinnerPriority.selectedItemPosition) {
            0 -> Priority.LOW
            1 -> Priority.MEDIUM
            2 -> Priority.HIGH
            else -> Priority.URGENT
        }

        val error = CategoryRepository.addCategory(
            name, description, priority
        )

        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        } else {
            binding.etCategoryName.text?.clear()
            binding.etCategoryDescription.text?.clear()
            Toast.makeText(
                requireContext(),
                "Category added successfully",
                Toast.LENGTH_SHORT
            ).show()
            displayCategories()
        }
    }

    private fun displayCategories() {
        binding.layoutCategories.removeAllViews()

        CategoryRepository.getCategories()
            .filter { it.isActive }
            .forEach { category ->
                val item = android.widget.TextView(requireContext()).apply {
                    text = "${category.name}\n${category.description}\nDefault priority: ${category.defaultPriority}"
                    textSize = 15f
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(android.graphics.Color.WHITE)
                }

                item.setOnClickListener {
                    val priorities = arrayOf("Low", "Medium", "High", "Urgent")

                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Change priority: ${category.name}")
                        .setItems(priorities) { _, selected ->
                            val newPriority = when (selected) {
                                0 -> Priority.LOW
                                1 -> Priority.MEDIUM
                                2 -> Priority.HIGH
                                else -> Priority.URGENT
                            }

                            val error = CategoryRepository.updateDefaultPriority(
                                category.id, newPriority
                            )

                            if (error != null) {
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                            } else {
                                displayCategories()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                binding.layoutCategories.addView(item)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
