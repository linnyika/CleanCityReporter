package com.group2.cleancity.ui.officer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group2.cleancity.databinding.ItemOfficerReportBinding
import com.group2.cleancity.model.Priority
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus

/**
 * RecyclerView Adapter responsible for efficiently rendering list items of reports in the Officer Queue.
 * Uses ViewHolder pattern to recycle item view layouts as the user scrolls.
 */
class OfficerReportAdapter(
    private var reports: List<Report>, // The list dataset of reports to display
    private val onItemClick: (Report) -> Unit // Higher-order function (lambda callback) executed on item tap
) : RecyclerView.Adapter<OfficerReportAdapter.ReportViewHolder>() {

    // Method to refresh dataset and notify RecyclerView to re-render views
    fun updateReports(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged() // Triggers UI redraw of modified items
    }

    // Called by RecyclerView when it needs a new ViewHolder instance (inflates item_officer_report.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemOfficerReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    // Called by RecyclerView to bind report data at a specific position to the ViewHolder
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    // Returns total count of items in the current dataset
    override fun getItemCount(): Int = reports.size

    // ViewHolder class holding references to layout views inside item_officer_report.xml
    inner class ReportViewHolder(private val binding: ItemOfficerReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Binds data fields from a single Report object into layout views
        fun bind(report: Report) {
            binding.tvReportId.text = report.id
            binding.tvCategory.text = report.categoryName
            binding.tvLocation.text = report.locationText
            binding.tvReporterName.text = "By: ${report.reporterName}"

            // Dynamically style Status Badge colors based on current ReportStatus enum value
            binding.tvStatusBadge.text = report.status.name
            when (report.status) {
                ReportStatus.SUBMITTED -> {
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E3F2FD")) // Light Blue
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#1976D2")) // Dark Blue
                }
                ReportStatus.ACKNOWLEDGED -> {
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF8E1")) // Light Amber
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#F57F17")) // Dark Amber
                }
                ReportStatus.IN_PROGRESS -> {
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9")) // Light Green
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32")) // Dark Green
                }
                ReportStatus.RESOLVED -> {
                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#F3E5F5")) // Light Purple
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#7B1FA2")) // Dark Purple
                }
            }

            // Dynamically style Priority Badge colors based on Priority enum value
            binding.tvPriorityBadge.text = report.priority.name
            when (report.priority) {
                Priority.LOW -> {
                    binding.tvPriorityBadge.setBackgroundColor(Color.parseColor("#ECEFF1")) // Grey
                    binding.tvPriorityBadge.setTextColor(Color.parseColor("#455A64"))
                }
                Priority.MEDIUM -> {
                    binding.tvPriorityBadge.setBackgroundColor(Color.parseColor("#FFF3E0")) // Light Orange
                    binding.tvPriorityBadge.setTextColor(Color.parseColor("#E65100")) // Dark Orange
                }
                Priority.HIGH, Priority.URGENT -> {
                    binding.tvPriorityBadge.setBackgroundColor(Color.parseColor("#FFEBEE")) // Light Red
                    binding.tvPriorityBadge.setTextColor(Color.parseColor("#C62828")) // Dark Red
                }
            }

            // Register click listener on card item root view
            binding.root.setOnClickListener {
                onItemClick(report) // Trigger lambda callback passed from Fragment
            }
        }
    }
}
