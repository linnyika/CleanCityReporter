package com.group2.cleancity.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.group2.cleancity.R
import com.group2.cleancity.data.CategoryRepository
import com.group2.cleancity.databinding.FragmentReportBinding
import com.group2.cleancity.model.Category
import com.group2.cleancity.data.ReportRepository
import com.group2.cleancity.data.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.navigation.fragment.findNavController
import java.io.File

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: Category? = null
    private var selectedPhotoUri: Uri? = null
    private var cameraPhotoUri: Uri? = null

    // Opens the gallery to select a photo
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {
                selectedPhotoUri = uri

                binding.ivReportPhoto.setImageURI(uri)
                binding.ivReportPhoto.visibility = View.VISIBLE
                binding.btnRemovePhoto.visibility = View.VISIBLE
                binding.photoError.visibility = View.GONE
            }
        }

    // Opens the camera and captures a photo
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->

            if (success) {
                selectedPhotoUri = cameraPhotoUri

                binding.ivReportPhoto.setImageURI(selectedPhotoUri)
                binding.ivReportPhoto.visibility = View.VISIBLE
                binding.btnRemovePhoto.visibility = View.VISIBLE
                binding.photoError.visibility = View.GONE
            } else {
                Toast.makeText(
                    requireContext(),
                    "Photo capture cancelled.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Requests camera permission when needed
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

            if (granted) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to take a photo. You can choose one from your gallery instead.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Creates a file for the camera photo and opens the camera
    private fun openCamera() {

        val photoDirectory = File(
            requireContext().getExternalFilesDir(null),
            "Pictures"
        )

        if (!photoDirectory.exists()) {
            photoDirectory.mkdirs()
        }

        val photoFile = File(
            photoDirectory,
            "report_${System.currentTimeMillis()}.jpg"
        )

        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        cameraLauncher.launch(cameraPhotoUri!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBackToDashboard.setOnClickListener {
            findNavController().navigateUp()
        }

        // Open the gallery
        binding.btnChooseGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Remove the selected photo
        binding.btnRemovePhoto.setOnClickListener {
            selectedPhotoUri = null
            cameraPhotoUri = null

            binding.ivReportPhoto.setImageURI(null)
            binding.ivReportPhoto.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
        }

        // Check camera permission before opening the camera
        binding.btnTakePhoto.setOnClickListener {

            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {

                    openCamera()
                }

                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        // Display available report categories
        displayCategories()
        binding.btnSubmitReport.setOnClickListener {

            if (!validateForm()) {
                return@setOnClickListener
            }

            val currentUser = SessionManager.currentUser

            if (currentUser == null) {
                Toast.makeText(
                    requireContext(),
                    "Please log in before submitting a report.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            try {

                val report = ReportRepository.saveReport(
                    residentId = currentUser.id.toString(),
                    category = selectedCategory!!,
                    description = binding.etDescription.text.toString().trim(),
                    photoUri = selectedPhotoUri.toString(),
                    location = binding.etLocation.text.toString().trim()
                )

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Report Submitted Successfully!")
                    .setMessage(
                        "Thank you for helping keep Nairobi clean.\n\n" +
                                "Your Report ID is: ${report.id}\n\n" +
                                "Keep this ID for future reference."
                    )
                    .setPositiveButton("Done") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

                // Clear the form after successful submission
                selectedCategory = null
                selectedPhotoUri = null
                cameraPhotoUri = null

                binding.etDescription.text?.clear()
                binding.etLocation.text?.clear()

                binding.ivReportPhoto.setImageURI(null)
                binding.ivReportPhoto.visibility = View.GONE
                binding.btnRemovePhoto.visibility = View.GONE

                binding.categoryContainer.clearFocus()

                displayCategories()

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message ?: "Unable to submit report. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun displayCategories() {

        val categories = CategoryRepository.getCategories()

        binding.categoryContainer.removeAllViews()

        categories.forEach { category ->

            val card = MaterialCardView(requireContext()).apply {

                radius = 12f * resources.displayMetrics.density
                cardElevation = 2f
                strokeWidth = 1

                strokeColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )

                setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.neutral)
                )

                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                params.bottomMargin = 12.dp()
                layoutParams = params

                isClickable = true
                isFocusable = true
            }

            val text = TextView(requireContext()).apply {

                this.text = "${category.name}\n${category.description}"
                textSize = 15f

                val padding = (16 * resources.displayMetrics.density).toInt()
                val horizontalPadding = (20 * resources.displayMetrics.density).toInt()

                setPadding(
                    horizontalPadding,
                    padding,
                    horizontalPadding,
                    padding
                )

                setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.secondary)
                )
            }

            card.addView(text)

            card.setOnClickListener {

                selectedCategory = category

                binding.categoryError.visibility = View.GONE

                updateSelectedCard(card)
            }

            binding.categoryContainer.addView(card)
        }
    }

    // Highlights the category selected by the resident
    private fun updateSelectedCard(selectedCard: MaterialCardView) {

        for (index in 0 until binding.categoryContainer.childCount) {

            val card = binding.categoryContainer.getChildAt(index)
                    as MaterialCardView

            card.strokeWidth = if (card == selectedCard) {
                3.dp()
            } else {
                1.dp()
            }
        }
    }
    // Validates all required report fields
    private fun validateForm(): Boolean {

        var isValid = true

        // Check category
        if (selectedCategory == null) {
            binding.categoryError.text = "Please select an issue category."
            binding.categoryError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.categoryError.visibility = View.GONE
        }

        // Check description
        val description = binding.etDescription.text.toString().trim()

        if (description.isEmpty()) {
            binding.descriptionError.text = "Please describe the sanitation issue."
            binding.descriptionError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.descriptionError.visibility = View.GONE
        }

        // Check photograph
        if (selectedPhotoUri == null) {
            binding.photoError.text = "Please attach a photograph."
            binding.photoError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.photoError.visibility = View.GONE
        }

        // Check location
        val location = binding.etLocation.text.toString().trim()

        if (location.isEmpty()) {
            binding.locationError.text = "Please enter the location."
            binding.locationError.visibility = View.VISIBLE
            isValid = false
        } else {
            binding.locationError.visibility = View.GONE
        }

        return isValid
    }
    // Converts dp values into pixels
    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
