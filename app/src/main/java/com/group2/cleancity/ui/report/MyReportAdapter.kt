package com.group2.cleancity.ui.report

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.group2.cleancity.databinding.ItemMyReportBinding
import com.group2.cleancity.model.Report
import com.group2.cleancity.model.ReportStatus

/** Binds resident-owned reports to the rows in the My Reports list. */
class MyReportAdapter(
    private val onReportClick: (String) -> Unit
) : ListAdapter<Report, MyReportAdapter.ReportViewHolder>(REPORT_DIFF_CALLBACK) {

    /** Submit snapshots so later repository status changes do not alter DiffUtil old items. */
    fun submitReports(reports: List<Report>) {
        submitList(reports.map { report -> report.copy() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemMyReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemMyReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.tvReportId.text = report.id
            binding.tvReportCategory.text = report.categoryName
            binding.tvReportLocation.text = report.locationText
            binding.tvReportTime.text = DateUtils.getRelativeTimeSpanString(
                report.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.tvReportStatus.text = report.status.name.replace("_", " ")

            // Reset the image first because this row view may have been recycled.
            binding.imageReportThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            if (!report.photoUri.isNullOrBlank()) {
                binding.imageReportThumbnail.setImageURI(Uri.parse(report.photoUri))
            }

            styleStatusBadge(report.status)

            binding.root.setOnClickListener {
                onReportClick(report.id)
            }
        }

        private fun styleStatusBadge(status: ReportStatus) {
            val backgroundColor = when (status) {
                ReportStatus.SUBMITTED -> Color.parseColor("#E3F2FD")
                ReportStatus.ACKNOWLEDGED -> Color.parseColor("#FFF8E1")
                ReportStatus.IN_PROGRESS -> Color.parseColor("#E8F5E9")
                ReportStatus.RESOLVED -> Color.parseColor("#F3E5F5")
            }
            val textColor = when (status) {
                ReportStatus.SUBMITTED -> Color.parseColor("#1976D2")
                ReportStatus.ACKNOWLEDGED -> Color.parseColor("#F57F17")
                ReportStatus.IN_PROGRESS -> Color.parseColor("#2E7D32")
                ReportStatus.RESOLVED -> Color.parseColor("#7B1FA2")
            }

            // Tint the rounded background and set the label color for this status.
            binding.tvReportStatus.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            binding.tvReportStatus.setTextColor(textColor)
        }
    }

    private companion object {
        val REPORT_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Report>() {
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean =
                oldItem == newItem
        }
    }
}